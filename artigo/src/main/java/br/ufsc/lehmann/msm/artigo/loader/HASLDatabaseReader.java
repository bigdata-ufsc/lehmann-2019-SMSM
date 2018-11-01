package br.ufsc.lehmann.msm.artigo.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.ThreeDimensionalPoint;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix.Tuple;
import br.ufsc.lehmann.msm.artigo.loader.HASLRecord.Hand;
import br.ufsc.lehmann.msm.artigo.loader.HASLRecord.Hands;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import smile.math.Math;

public class HASLDatabaseReader implements IDataReader {
	
	private static int semantic_count = 3;

	public static final BasicSemantic<ThreeDimensionalPoint> LEFT_SPATIAL = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<ThreeDimensionalPoint> RIGHT_SPATIAL = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> LEFT_X = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> LEFT_Y = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> LEFT_Z = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> RIGHT_X = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> RIGHT_Y = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Double> RIGHT_Z = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Tuple<ThreeDimensionalPoint, ThreeDimensionalPoint>> BOTH_SPATIAL = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Hand> LEFT_HAND = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Hand> RIGHT_HAND = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<Hands> BOTH_HANDS = new BasicSemantic<>(semantic_count++);
	public static final BasicSemantic<String> WORD = new BasicSemantic<>(semantic_count++);
	
	private static final Semantic[] ALL_SEMANTICS = new Semantic[] {
			Semantic.GID,
			Semantic.SPATIAL,
			Semantic.TEMPORAL,
			WORD,
			LEFT_SPATIAL,
			LEFT_X,
			LEFT_Y,
			LEFT_Z,
			RIGHT_X,
			RIGHT_Y,
			RIGHT_Z,
			RIGHT_SPATIAL,
			BOTH_SPATIAL,
			LEFT_HAND,
			RIGHT_HAND,
			BOTH_HANDS,
	};

	private boolean raw;

	private boolean normalized;
	private Boolean leftHand;
	private Boolean rightHand;
	private boolean shifted;

	public HASLDatabaseReader(boolean raw, boolean shifted, boolean normalized, Boolean leftHand, Boolean rightHand) {
		this.raw = raw;
		this.shifted = shifted;
		this.normalized = normalized;
		this.leftHand = leftHand;
		this.rightHand = rightHand;
	}

	@Override
	public List<SemanticTrajectory> read() {
		try {
			return readRaw();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private List<SemanticTrajectory> readRaw() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "asl.high_quality_asl", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT"
				+ " \"gid\",\"tid\",\"author\",\"class\",\"lx\",\"ly\",\"lz\",\"lroll\",\"lpitch\",\"lyaw\",\"lthumb\",\"lfore\",\"lmiddle\",\"lring\",\"llittle\",\"rx\",\"ry\",\"rz\",\"rroll\",\"rpitch\",\"ryaw\",\"rthumb\",\"rfore\",\"rmiddle\",\"rring\",\"rlittle\""
				+ " FROM \"asl\".\"high_quality_asl\"");
		PreparedStatement st = conn.prepareStatement(sb.toString());
		st.setFetchSize(1000);

		ResultSet data = st.executeQuery();
		Multimap<Integer, HASLRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			records.put(data.getInt("tid"), 
					new HASLRecord(
							data.getInt("gid"),
							data.getInt("tid"),
							data.getString("class"),
							data.getString("author"),
							data.getDouble("lx"),data.getDouble("ly"),data.getDouble("lz"),data.getDouble("lroll"),data.getDouble("lpitch"),data.getDouble("lyaw"),data.getDouble("lthumb"),data.getDouble("lfore"),data.getDouble("lmiddle"),data.getDouble("lring"),data.getDouble("llittle"),
							data.getDouble("rx"),data.getDouble("ry"),data.getDouble("rz"),data.getDouble("rroll"),data.getDouble("rpitch"),data.getDouble("ryaw"),data.getDouble("rthumb"),data.getDouble("rfore"),data.getDouble("rmiddle"),data.getDouble("rring"),data.getDouble("rlittle")));
		}
		System.out.printf("Loaded %d GPS points from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<Integer> keys = records.keySet();
		DescriptiveStatistics trajLenghtStats = new DescriptiveStatistics();
		DescriptiveStatistics trajPointStats = new DescriptiveStatistics();
		Map<Semantic, AggregateSummaryStatistics> semanticStats = new HashMap<>();
		for (int k = 0; k < ALL_SEMANTICS.length; k++) {
			semanticStats.put(ALL_SEMANTICS[k], new AggregateSummaryStatistics());
		}
		for (Integer trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajId, semantic_count);
			Collection<HASLRecord> collection = records.get(trajId);

			int i = 0;
			HASLRecord first = collection.iterator().next();
			Map<String, double[]> dimensionValues = new HashMap<>();
			dimensionValues.put("lx", new double[collection.size()]);
			dimensionValues.put("ly", new double[collection.size()]);
			dimensionValues.put("lz", new double[collection.size()]);
			dimensionValues.put("rx", new double[collection.size()]);
			dimensionValues.put("ry", new double[collection.size()]);
			dimensionValues.put("rz", new double[collection.size()]);
			int j = 0;
			for (Iterator iterator = collection.iterator(); iterator.hasNext();j++) {
				HASLRecord record = (HASLRecord) iterator.next();
				dimensionValues.get("lx")[j] = record.getLx();
				dimensionValues.get("ly")[j] = record.getLy();
				dimensionValues.get("lz")[j] = record.getLz();
				dimensionValues.get("rx")[j] = record.getRx();
				dimensionValues.get("ry")[j] = record.getRy();
				dimensionValues.get("rz")[j] = record.getRz();
			}
			if(normalized) {
				Math.standardize(dimensionValues.get("lx"));
				Math.standardize(dimensionValues.get("ly"));
				Math.standardize(dimensionValues.get("lz"));
				Math.standardize(dimensionValues.get("rx"));
				Math.standardize(dimensionValues.get("ry"));
				Math.standardize(dimensionValues.get("rz"));
			} else if (shifted) {
				shiftToOrigin(dimensionValues.get("lx"));
				shiftToOrigin(dimensionValues.get("ly"));
				shiftToOrigin(dimensionValues.get("lz"));
				shiftToOrigin(dimensionValues.get("rx"));
				shiftToOrigin(dimensionValues.get("ry"));
				shiftToOrigin(dimensionValues.get("rz"));
			}
			DescriptiveStatistics pointsStats = new DescriptiveStatistics();
			Instant previousInstant = null;
			TPoint previousPoint = null;
			j = 0;
			for (Iterator iterator = collection.iterator(); iterator.hasNext();j++) {
				HASLRecord record = (HASLRecord) iterator.next();
				Instant instant = Instant.ofEpochMilli(i);
				double lx = dimensionValues.get("lx")[j];
				double ly = dimensionValues.get("ly")[j];
				double lz = dimensionValues.get("lz")[j];
				ThreeDimensionalPoint lpoint = new ThreeDimensionalPoint(lx, ly, lz, new Timestamp(instant.toEpochMilli()));
				double rx = dimensionValues.get("rx")[j];;
				double ry = dimensionValues.get("ry")[j];
				double rz = dimensionValues.get("rz")[j];
				ThreeDimensionalPoint rpoint = new ThreeDimensionalPoint(rx, ry, rz, new Timestamp(instant.toEpochMilli()));
				Hand left = new Hand(lx, ly, lz, record.getLroll(), record.getLpitch(), record.getLyaw(), record.getLthumb(), record.getLfore(), record.getLmiddle(), record.getLring(), record.getLlittle());
				Hand right = new Hand(rx, ry, rz, record.getRroll(), record.getRpitch(), record.getRyaw(), record.getRthumb(), record.getRfore(), record.getRmiddle(), record.getRring(), record.getRlittle());
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.SPATIAL, leftHand ? lpoint.to2D() : rpoint.to2D());
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(instant, instant));
				s.addData(i, WORD, record.getClazz());
				s.addData(i, LEFT_SPATIAL, lpoint);
				s.addData(i, LEFT_X, lx);
				s.addData(i, LEFT_Y, ly);
				s.addData(i, LEFT_Z, lz);
				s.addData(i, RIGHT_X, rx);
				s.addData(i, RIGHT_Y, ry);
				s.addData(i, RIGHT_Z, rz);
				s.addData(i, RIGHT_SPATIAL, rpoint);
				s.addData(i, BOTH_SPATIAL, new Tuple<>(lpoint, rpoint));
				s.addData(i, LEFT_HAND, left);
				s.addData(i, RIGHT_HAND, right);
				s.addData(i, BOTH_HANDS, new Hands(left, right));
				i++;
				if(previousPoint != null) {
					pointsStats.addValue(Semantic.SPATIAL_EUCLIDEAN.distance(previousPoint, leftHand ? lpoint.to2D() : rpoint.to2D()));
				}
				previousInstant = instant;
				previousPoint = leftHand ? lpoint.to2D() : rpoint.to2D();
			}
			for (int k = 0; k < ALL_SEMANTICS.length; k++) {
				SummaryStatistics trajStats = semanticStats.get(ALL_SEMANTICS[k]).createContributingStatistics();
				for (int m = 0; m < s.length() - 1; m++) {
					Object p1 = ALL_SEMANTICS[k].getData(s, m);
					Object p2 = ALL_SEMANTICS[k].getData(s, m + 1);
					Object distance = ALL_SEMANTICS[k].distance(p1, p2);
					if(distance instanceof Number) {
						trajStats.addValue(((Number) distance).doubleValue());
					}
				}
				if(trajStats.getN() > 1) {
					s.setLocalStats(ALL_SEMANTICS[k], trajStats);
				}
			}
			trajLenghtStats.addValue(s.length());
			trajPointStats.addValue(pointsStats.getMean());
			ret.add(s);
		}
		for (SemanticTrajectory traj : ret) {
			for (int k = 0; k < ALL_SEMANTICS.length; k++) {
				traj.setGlobalStats(ALL_SEMANTICS[k], semanticStats.get(ALL_SEMANTICS[k]).getSummary());
			}
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f, mean distance between point - %.2f\n", trajLenghtStats.getMean(), trajLenghtStats.getMin(), trajLenghtStats.getMax(), trajLenghtStats.getStandardDeviation(), trajPointStats.getPercentile(50));
		return ret;
	}

	private void shiftToOrigin(double[] column) {
		if(column == null || column.length == 0) {
			return;
		}
		double firstValue = column[0];
		for (int i = 0; i < column.length; i++) {
			column[i] -= firstValue;
		}
	}

	private double getRz(HASLRecord first, HASLRecord record) {
		return record.getRz() - (shifted ? first.getRz() : 0);
	}

	private double getRy(HASLRecord first, HASLRecord record) {
		return record.getRy() - (shifted ? first.getRy() : 0);
	}

	private double getRx(HASLRecord first, HASLRecord record) {
		return record.getRx() - (shifted ? first.getRx() : 0);
	}

	private double getLz(HASLRecord first, HASLRecord record) {
		return record.getLz() - (shifted ? first.getLz() : 0);
	}

	private double getLy(HASLRecord first, HASLRecord record) {
		return record.getLy() - (shifted ? first.getLy() : 0);
	}

	private double getLx(HASLRecord first, HASLRecord record) {
		return record.getLx() - (shifted ? first.getLx() : 0);
	}
}
