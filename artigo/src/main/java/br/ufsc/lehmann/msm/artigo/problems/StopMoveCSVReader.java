package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;

public class StopMoveCSVReader {
	static final DateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Map<Integer, Move> moveCsvRead(CSVParser movesParser, Map<Integer, Stop> stops) throws IOException, ParseException {
		List<CSVRecord> records;
		Map<Integer, Move> moves = new HashMap<>();
		records = movesParser.getRecords();
		Iterator<CSVRecord> movesData = records.subList(1, records.size()).iterator();
		while(movesData.hasNext()) {
			CSVRecord data = movesData.next();
			int moveId = Integer.parseInt(data.get("move_id"));
			Move move = moves.get(moveId);
			if (move == null) {
				String startStopId = data.get("start_stop_id");
				String endStopId = data.get("end_stop_id");
				move = new Move(moveId, //
						StringUtils.isEmpty(startStopId) ? null : stops.get(Integer.parseInt(startStopId)), //
						StringUtils.isEmpty(endStopId)? null : stops.get(Integer.parseInt(endStopId)), //
						TIMESTAMP.parse(data.get("start_time")).getTime(), //
						TIMESTAMP.parse(data.get("end_time")).getTime(), //
						Integer.parseInt(data.get("begin")), //
						Integer.parseInt(data.get("length")), //
						null);
				moves.put(moveId, move);
			}
		}
		return moves;
	}

	public static Map<Integer, Stop> stopsCsvRead(CSVParser stopsParser) throws IOException, ParseException {
		List<CSVRecord> records = stopsParser.getRecords();
		Iterator<CSVRecord> stopsData = records.subList(1, records.size()).iterator();
		Map<Integer, Stop> stops = new HashMap<>();
		while (stopsData.hasNext()) {
			CSVRecord data = stopsData.next();
			int stopId = Integer.parseInt(data.get("stop_id"));
			Stop stop = stops.get(stopId);
			if (stop == null) {
				stop = new Stop(stopId, null, //
						TIMESTAMP.parse(data.get("start_time")).getTime(), //
						TIMESTAMP.parse(data.get("end_time")).getTime(), //
						new TPoint(Double.parseDouble(data.get("start_lat")), Double.parseDouble(data.get("start_lon"))), //
						Integer.parseInt(data.get("begin")), //
						new TPoint(Double.parseDouble(data.get("end_lat")), Double.parseDouble(data.get("end_lon"))), //
						Integer.parseInt(data.get("length")), //
						new TPoint(Double.parseDouble(data.get("centroid_lat")), Double.parseDouble(data.get("centroid_lon"))),//
						data.get("street")//
				);
				stops.put(stopId, stop);
			}
		}
		return stops;
	}
}
