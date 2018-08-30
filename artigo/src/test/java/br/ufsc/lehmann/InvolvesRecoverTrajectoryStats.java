package br.ufsc.lehmann;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;

public class InvolvesRecoverTrajectoryStats {

	private static final String SCHEMA = InvolvesDatabaseReader.SCHEMA;

	public List<TrajectoryStats> recoverStats()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "public.amsterdan_park_cbsmot", null, null);
		DataRetriever retriever = source.getRetriever();
		Connection conn = retriever.getConnection();
		try {
			Statement st = conn.createStatement();
			st.setFetchSize(1000);
			
			StringBuffer sb = new StringBuffer();
			
			sb.append("    SELECT rank_filter.* FROM ( "); 
			sb.append("        SELECT \"id_colaborador_unidade\",\"id_dimensao_data\",\"is_better_trajectory\", lcss_to_better, ed_from_better,stops, "); 
			sb.append("        rank() OVER ( "); 
			sb.append("            PARTITION BY id_colaborador_unidade "); 
			sb.append("            ORDER BY lcss_to_better DESC, ed_from_better, length(stops) "); 
			sb.append("        ) "); 
			sb.append("        FROM " + SCHEMA + ".\"trajectory_stats_FastCBSMoT_com_auditoria_100mts_30_mins\" "); 
			sb.append("        where lcss_to_better > 1 "); 
			sb.append("    ) rank_filter "); 
			sb.append("            ORDER BY id_colaborador_unidade, is_better_trajectory, lcss_to_better DESC, ed_from_better, length(stops), rank_filter.rank ");
			
			ResultSet rs = st.executeQuery(sb.toString());
			List<TrajectoryStats> stats = new ArrayList<>();
			while(rs.next()) {
				String[] r = new String[7];
				for (int i = 0; i < 7; i++) {
					r[i] = rs.getString(i + 1);
				}
				stats.add(new TrajectoryStats(r));
			}
			return stats;
		} finally {
			conn.close();
		}
	}
	
	public List<SemanticTrajectory> recoverBestTrajectories(SemanticTrajectory[] allData) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		List<TrajectoryStats> stats = recoverStats().stream().filter((t) -> t.betterTrajectory).collect(Collectors.toList());
		
		List<SemanticTrajectory> bestTrajectories = Arrays//
														.stream(allData)//
														.filter((t) -> filterStats(stats, InvolvesDatabaseReader.USER_ID.getData(t, 0), InvolvesDatabaseReader.DIMENSAO_DATA.getData(t, 0)))//
														.collect(Collectors.toList());
		return bestTrajectories;
	}

	public Map<Integer, GroundtruthRanking> getRanking() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		List<TrajectoryStats> stats = recoverStats();
		Map<Integer, GroundtruthRanking> ret = new HashMap<>();
		stats.stream().mapToInt(TrajectoryStats::getUserId).distinct().forEach(userId -> ret.put(userId, new GroundtruthRanking()));
		stats.stream().forEach(stat -> ret.get(stat.userId).addRankingEntry(stat));
		return ret;
	}
	
	private boolean filterStats(List<TrajectoryStats> stats, Integer userId, Integer dimensaoData) {
		Optional<TrajectoryStats> first = stats.stream().filter(s -> s.dimensaoData.intValue() == dimensaoData.intValue() && s.userId.intValue() == userId.intValue()).findFirst();
		return first.isPresent();
	}

	static class TrajectoryStats {
		
		@Override
		public String toString() {
			return "TrajectoryStats [userId=" + userId + ", dimensaoData=" + dimensaoData + ", rank=" + rank
					+ ", betterTrajectory=" + betterTrajectory + "]";
		}

		private Integer userId;
		private Integer dimensaoData;
		private Integer rank;
		private boolean betterTrajectory;

		public TrajectoryStats(String[] stat) {
			userId = Integer.valueOf(stat[0]);
			dimensaoData = Integer.valueOf(stat[1]);
			betterTrajectory = "t".equals(stat[2]);
			rank = Integer.valueOf(stat[6]);
		}

		public Integer getRank() {
			return rank;
		}

		public Integer getUserId() {
			return userId;
		}

		public Integer getDimensaoData() {
			return dimensaoData;
		}

		public boolean isBetterTrajectory() {
			return betterTrajectory;
		}
	}
	
	static class GroundtruthRanking {
		
		@Override
		public String toString() {
			return "GroundtruthRanking [positions=" + positions + "]";
		}

		private Map<Integer, RankingPosition> positions = new HashMap<>();

		public void addRankingEntry(TrajectoryStats stats) {
			RankingPosition pos = positions.computeIfAbsent(stats.rank, k -> new RankingPosition(stats.rank));
			pos.add(stats);
			Optional<Integer> findFirst = positions.keySet().stream().sorted().filter(i -> i > pos.startPosition).findFirst();
			if(findFirst.isPresent()) {
				pos.endPosition = findFirst.get();
			}
			findFirst = positions.keySet().stream().sorted(Collections.reverseOrder()).filter(i -> i < pos.startPosition).findFirst();
			if(findFirst.isPresent()) {
				positions.get(findFirst.get()).endPosition = pos.startPosition;
			}
		}

		public boolean hasTrajectorynRanking(
				Integer userId,
				Integer dimensaoData) {
			for (RankingPosition pos : positions.values()) {
				if(pos.isTrajectorynRanking(userId, dimensaoData)) {
					return true;
				}
			}
			return false;
		}

		public RankingPosition getTrajectorynRanking(
				Integer userId,
				Integer dimensaoData) {
			for (RankingPosition pos : positions.values()) {
				if(pos.isTrajectorynRanking(userId, dimensaoData)) {
					return pos;
				}
			}
			return null;
		}

		public boolean isInRankingRanging(Integer userId, Integer dimensaoData, int position) {
			for (RankingPosition pos : positions.values()) {
				if(pos.isTrajectorynRanking(userId, dimensaoData)) {
					if(pos.isInRankingRanging(position)) {
						return true;
					}
				}
			}
			return false;
		}

		public RankingPosition maxRanking() {
			return positions.get(positions.keySet().stream().sorted(Collections.reverseOrder()).findFirst().get());
		}

		public List<TrajectoryStats> getRankedTrajectories() {
			List<Integer> keyPositions = positions.keySet().stream().sorted().collect(Collectors.toList());
			List<TrajectoryStats> ret = new ArrayList<>();
			for (Integer pos : keyPositions) {
				RankingPosition rankingPosition = positions.get(pos);
				ret.addAll(rankingPosition.trajs);
			}
			return ret;
		}
		
	}

	static class RankingPosition {

		@Override
		public String toString() {
			return "RankingPosition [startPosition=" + startPosition + ", endPosition=" + endPosition + ", trajs="
					+ trajs + "]";
		}

		private Integer startPosition;
		private Integer endPosition;
		private List<TrajectoryStats> trajs;

		public RankingPosition(Integer startPosition) {
			this.startPosition = startPosition;
			this.trajs = new ArrayList<>();
		}

		public boolean isInRankingRanging(Integer position) {
			return position >= startPosition && position < (endPosition != null ? endPosition : Integer.MAX_VALUE);
		}

		public boolean isTrajectorynRanking(Integer userId, Integer dimensaoData) {
			for (TrajectoryStats stats : trajs) {
				if(stats.userId.intValue() == userId.intValue() &&
						stats.dimensaoData.intValue() == dimensaoData.intValue()) {
					return true;
				}
			}
			return false;
		}

		public void add(TrajectoryStats stat) {
			trajs.add(stat);
		}

		public Integer getStartPosition() {
			return startPosition;
		}

		public Integer getEndPosition() {
			return endPosition;
		}

		public List<TrajectoryStats> getTrajs() {
			return trajs;
		}

	}
}
