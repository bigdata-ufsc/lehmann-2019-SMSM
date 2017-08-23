package br.ufsc.lehmann.prototype;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;

public class PrototypeDataReader {
	
	public static final BasicSemantic<Object> USER_ID = new BasicSemantic<>(5);
	
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(3, new AttributeDescriptor<Stop>(AttributeType.STOP_NAME, new EqualsDistanceFunction()));
	
	public static final MoveSemantic MOVE_SEMANTIC = new MoveSemantic(4, new AttributeDescriptor<Move>(AttributeType.MOVE_STREET_NAME, new EqualsDistanceFunction()));

	public List<SemanticTrajectory> read() throws IOException {
		CSVParser prototypeParser = CSVParser.parse(//
				new File("./src/test/resources/prototype.traj"), Charset.defaultCharset(),//
				CSVFormat.EXCEL.withDelimiter(';'));
		List<CSVRecord> records = new ArrayList<>(prototypeParser.getRecords());
		List<SemanticTrajectory> ret = new ArrayList<>();
		SemanticTrajectory current = null;
		int stopCount = 0, moveCount = 0;
		Map<Integer, Stop> stops = null;
		CSVRecord record = records.remove(0);
		while(!records.isEmpty()) {
			if(record.size() == 3) {
				current = new SemanticTrajectory(record.get(0), 6);
				ret.add(current);
				stopCount = Integer.parseInt(record.get(1));
				moveCount = Integer.parseInt(record.get(2));
				stops = new HashMap<>();
				record = records.remove(0);
			} else {
				while(stopCount > 0) {
					int stopId = Integer.parseInt(record.get(0));
					String startPoint = record.get(1);
					String endPoint = record.get(2);
					String interval = record.get(3);
					Stop s = new Stop(stopId, record.get(4), 
							/*startTime*/Integer.parseInt(interval.substring(0, interval.indexOf('-'))),
							/*endTime*/Integer.parseInt(interval.substring(interval.indexOf('-') + 1)), 
							/*startPoint*/new TPoint(Integer.parseInt(startPoint.substring(0, startPoint.indexOf(','))), Integer.parseInt(startPoint.substring(startPoint.indexOf(',') + 1))), 
							stopId, 
							/*endPoint*/new TPoint(Integer.parseInt(endPoint.substring(0, endPoint.indexOf(','))), Integer.parseInt(endPoint.substring(endPoint.indexOf(',') + 1))),
							1, 
							/*centroid*/new TPoint(Integer.parseInt(startPoint.substring(0, startPoint.indexOf(','))), Integer.parseInt(startPoint.substring(startPoint.indexOf(',') + 1))), 
							null);
					current.addData(stopId, STOP_SEMANTIC, s);
					current.addData(stopId, Semantic.GID, stopId);
					current.addData(stopId, Semantic.GEOGRAPHIC_EUCLIDEAN, //
							new TPoint(Integer.parseInt(startPoint.substring(0, startPoint.indexOf(','))), Integer.parseInt(startPoint.substring(startPoint.indexOf(',') + 1)))//
						);
					current.addData(stopId, Semantic.TEMPORAL, new TemporalDuration(//
							Instant.ofEpochMilli(Integer.parseInt(interval.substring(0, interval.indexOf('-')))),// 
							Instant.ofEpochMilli(Integer.parseInt(interval.substring(interval.indexOf('-') + 1)))//
						));
					current.addData(stopId, USER_ID, current.getTrajectoryId());
					stops.put(stopId, s);
					if(stopCount-- > 0) {
						record = records.remove(0);
					}
				}
				while(moveCount > 0) {
					int moveId = Integer.parseInt(record.get(0));
					Stop start = stops.get(Integer.parseInt(record.get(1)));
					Stop end = stops.get(Integer.parseInt(record.get(2)));
					String[] pointsRaw = record.get(3).substring(1, record.get(3).length() - 1).split(",");
					TPoint[] points = new TPoint[pointsRaw.length];
					for (int i = 0; i < pointsRaw.length; i++) {
						points[i] = new TPoint(Integer.parseInt(pointsRaw[i].substring(0, pointsRaw[i].indexOf(' '))), Integer.parseInt(pointsRaw[i].substring(pointsRaw[i].indexOf(' ') + 1)));
					}
					String streetName = record.get(4);
					Move move = new Move(moveId, start, end, start.getEndTime(), end.getStartTime(), start.getBegin() + start.getLength(), end.getBegin() - (start.getBegin() + start.getLength()), null, 0, 0, streetName);
					current.addData(moveId, MOVE_SEMANTIC, move);
					current.addData(moveId, Semantic.GID, moveId);
					current.addData(moveId, Semantic.GEOGRAPHIC_EUCLIDEAN, //
							points[0]//
						);
					current.addData(moveId, Semantic.TEMPORAL, new TemporalDuration(//
							Instant.ofEpochMilli(start.getEndTime()),// 
							Instant.ofEpochMilli(end.getStartTime())//
						));
					current.addData(moveId, USER_ID, current.getTrajectoryId());
					if(moveCount-- > 0 && !records.isEmpty()) {
						record = records.remove(0);
					}
				}
			}
		}
		return ret;
	}
}
