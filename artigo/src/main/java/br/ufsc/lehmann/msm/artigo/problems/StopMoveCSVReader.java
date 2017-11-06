package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;

public class StopMoveCSVReader {
	static final String TIMESTAMP_US = ("yyyy-MM-dd HH:mm:ss");
	static final String TIMESTAMP_BR = ("dd/MM/yyyy HH:mm");
	static final String TIMESTAMP = TIMESTAMP_US;

	public static Map<Integer, Move> moveCsvRead(CSVParser movesParser, Map<Integer, Stop> stops) throws IOException, ParseException {
		return moveCsvRead(movesParser, stops, TIMESTAMP_US, TIMESTAMP_BR);
	}

	public static Map<Integer, Move> moveCsvRead(CSVParser movesParser, Map<Integer, Stop> stops, String... timeFormat) throws IOException {
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
				try {
					move = new Move(moveId, //
							StringUtils.isEmpty(startStopId) ? null : stops.get(Integer.parseInt(startStopId)), //
							StringUtils.isEmpty(endStopId)? null : stops.get(Integer.parseInt(endStopId)), //
							DateUtils.parseDate(data.get("start_time"), timeFormat).getTime(), //
							DateUtils.parseDate(data.get("end_time"), timeFormat).getTime(), //
							Integer.parseInt(data.get("begin")), //
							Integer.parseInt(data.get("length")), //
							null);
				} catch (NumberFormatException | ParseException e) {
					throw new RuntimeException(e);
				}
				moves.put(moveId, move);
			}
		}
		return moves;
	}

	public static Map<Integer, Stop> stopsCsvRead(CSVParser stopsParser) throws IOException, ParseException {
		return stopsCsvRead(stopsParser, TIMESTAMP);
	}

	public static Map<Integer, Stop> stopsCsvRead(CSVParser stopsParser, String... timeFormat) throws IOException {
		return stopsCsvRead(stopsParser, EMPTY_CALLBACK, timeFormat);
	}
	
	public static Map<Integer, Stop> stopsCsvRead(CSVParser stopsParser, StopReaderCallback callback, String... timeFormat) throws IOException {
		List<CSVRecord> records = stopsParser.getRecords();
		Iterator<CSVRecord> stopsData = records.subList(1, records.size()).iterator();
		Map<Integer, Stop> stops = new HashMap<>();
		while (stopsData.hasNext()) {
			CSVRecord data = stopsData.next();
			int stopId = Integer.parseInt(data.get("stop_id"));
			Stop stop = stops.get(stopId);
			if (stop == null) {
				try {
					stop = new Stop(stopId, null, //
							parseDate(data.get("start_time"), timeFormat), //
							parseDate(data.get("end_time"), timeFormat), //
							new TPoint(Double.parseDouble(data.get("start_lat")), Double.parseDouble(data.get("start_lon"))), //
							Integer.parseInt(data.get("begin")), //
							new TPoint(Double.parseDouble(data.get("end_lat")), Double.parseDouble(data.get("end_lon"))), //
							Integer.parseInt(data.get("length")), //
							new TPoint(Double.parseDouble(data.get("centroid_lat")), Double.parseDouble(data.get("centroid_lon"))),//
							StringUtils.trim(data.get("street"))//
					);
					callback.readFields(stop, data);
				} catch (NumberFormatException e) {
					throw new RuntimeException(e);
				}
				stops.put(stopId, stop);
			}
		}
		return stops;
	}

	private static long parseDate(String string, String[] timeFormat) {
		for (int i = 0; i < timeFormat.length; i++) {
			try {
				TemporalAccessor parse = DateTimeFormatter.ofPattern(timeFormat[i]).parse(string);
				return parse.getLong(ChronoField.MILLI_OF_DAY) + (parse.getLong(ChronoField.EPOCH_DAY) * 24 * 60 * 60 * 1000);
			} catch (DateTimeParseException e) {
				//
			}
		}
		throw new IllegalArgumentException();
	}
	
	public static interface StopReaderCallback {
		void readFields(Stop stop, CSVRecord data);
	}
	private static final StopReaderCallback EMPTY_CALLBACK = new StopReaderCallback() {

		@Override
		public void readFields(Stop stop, CSVRecord data) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
