package br.ufsc.lehmann.msm.artigo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;

public class BikeDataReader {

	public static final BasicSemantic<String> USER = new BasicSemantic<>(2);
	public static final BasicSemantic<String> GENDER = new BasicSemantic<>(3);
	public static final BasicSemantic<String> BIRTH_YEAR = new BasicSemantic<>(4);
	
	private final class CSVComparator implements Comparator<CSVRecord> {
		private final Integer bikeid;
		private final DateFormat BIKE_FORMAT;

		private CSVComparator(Integer bikeid, DateFormat bIKE_FORMAT) {
			this.bikeid = bikeid;
			this.BIKE_FORMAT = bIKE_FORMAT;
		}

		public int compare(CSVRecord o1, CSVRecord o2) {
			String o1Start = o1.get("starttime");
			String o2Start = o2.get("starttime");
			try {
				return BIKE_FORMAT.parse(o1Start).compareTo(BIKE_FORMAT.parse(o2Start));
			} catch (ParseException | NumberFormatException e) {
				String o1end = o1.get("stoptime");
				String o2end = o2.get("stoptime");
				System.err.println("Bike - " + bikeid + ", dates - " + o1Start + " to " + o1end);
				System.err.println("Bike - " + bikeid + ", dates - " + o2Start + " to " + o2end);
				return -1;
			}
		}
	}

	private static final DateFormat CLIMATE_DATE_PARSER = new SimpleDateFormat("dd-MM-yyyy HH:mm Z", Locale.US);

	public List<SemanticTrajectory> read() throws IOException, InterruptedException {
		CSVParser bikeParser = CSVParser.parse(//
				new File("./src/main/resources/Bike_Data/NYC/Bike-NYC.csv"), Charset.defaultCharset(),//
				CSVFormat.EXCEL.withHeader("tripduration","starttime","stoptime","start station id","start station name","start station latitude","start station longitude","end station id","end station name","end station latitude","end station longitude","bikeid","usertype","birth year","gender"));
		CSVParser climateParser = CSVParser.parse(//
				new File("./src/main/resources/Bike_Data/NYC/Meteorology-NYC.csv"), Charset.defaultCharset(),//
				CSVFormat.EXCEL.withHeader("Date","Temperature","Wind Speed mph","Weather"));

		List<CSVRecord> bikeCsvRecords = bikeParser.getRecords();
		List<CSVRecord> climateCsvRecords = climateParser.getRecords();

		ConcurrentHashMap<Date, CSVRecord> climates = new ConcurrentHashMap<>(climateCsvRecords.size());
		List<Date> refDates = new ArrayList<>(climateCsvRecords.size());
		for (int i = 1; i < climateCsvRecords.size(); i++) {
			CSVRecord rec = climateCsvRecords.get(i);
			try {
				Date parsed = CLIMATE_DATE_PARSER.parse(rec.get("Date"));
				refDates.add(parsed);
				climates.put(parsed, rec);
			} catch (ParseException e) {
				throw new IOException(e);
			}
		}
		Collections.sort(refDates);

		MultiValuedMap<Integer, CSVRecord> multiMap = new ArrayListValuedHashMap<Integer, CSVRecord>();
		for (int i = 1; i < bikeCsvRecords.size(); i++) {
			CSVRecord record = bikeCsvRecords.get(i);
			Integer bikeid = Integer.parseInt(record.get("bikeid"));
			multiMap.put(bikeid, record);
		}
		final MultiValuedMap<Integer, CSVRecord> trajectories = new ArrayListValuedHashMap<Integer, CSVRecord>();
		final AtomicInteger lastTrajectoryId = new AtomicInteger(0);
		ExecutorService service = Executors.newFixedThreadPool(7);
		final float totalBikes = multiMap.keySet().size();
		int index = 0;
		for (Iterator<Integer> iterator = multiMap.keySet().iterator(); iterator.hasNext();) {
			final int i = ++index;
			final Integer bikeid = iterator.next();
			final List<CSVRecord> records = new ArrayList<CSVRecord>(multiMap.get(bikeid));
			service.execute(new Runnable() {
				
				ThreadLocal<DateFormat> FORMATTER = new ThreadLocal<>();
				
				private DateFormat getFormat() {
					if(FORMATTER.get() == null) {
						FORMATTER.set(new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US));
					}
					return FORMATTER.get();
				}
				
				public void run() {
					final DateFormat BIKE_FORMAT = getFormat();
					System.out.println(((i / totalBikes) * 100) + "% de " + totalBikes);
					Collections.sort(records, new CSVComparator(bikeid, BIKE_FORMAT));
					String previousStation = null;
					int lastTrajectoryIdLocal = 0;
					for (CSVRecord record : records) {
						if(previousStation == null || !record.get("start station id").equals(previousStation)) {
							lastTrajectoryIdLocal = lastTrajectoryId.incrementAndGet();
						}
						previousStation = record.get("end station id");
						trajectories.put(lastTrajectoryIdLocal, record);
					}
				}
			});
		}
		service.shutdown();
		service.awaitTermination(10, TimeUnit.MINUTES);
		Set<Integer> keySet = trajectories.keySet();
		System.out.println("Trajectories: " + keySet.size());
		List<SemanticTrajectory> ret = new ArrayList<>(keySet.size());
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		ClimateTemperatureSemantic tempSemantic = new ClimateTemperatureSemantic(5, .5);
		ClimateWindSpeedSemantic windSemantic = new ClimateWindSpeedSemantic(6, .1);
		ClimateWeatherSemantic weatherSemantic = new ClimateWeatherSemantic(7);
		for (Integer trajectoryId : keySet) {
			Collection<CSVRecord> records = trajectories.get(trajectoryId);
			records = new ArrayList<>(records);
			Collections.sort((List<CSVRecord>) records, new Comparator<CSVRecord>() {

				@Override
				public int compare(CSVRecord o1, CSVRecord o2) {
					try {
						Date start1 = dateFormat.parse(o1.get("starttime"));
						Date start2 = dateFormat.parse(o2.get("starttime"));
						return start1.compareTo(start2);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
			});
			SemanticTrajectory t = new SemanticTrajectory(trajectoryId, 8);
			int elementId = 0;
			CSVRecord lastRecord = null;
			for (CSVRecord record : records) {
				double lat, lon;
				lat = Double.parseDouble(record.get("start station latitude"));
				lon = Double.parseDouble(record.get("start station longitude"));
				t.addData(elementId, Semantic.GEOGRAPHIC, new TPoint(lat, lon));
				try {
					String st = record.get("starttime");
					Date start = dateFormat.parse(st);
					String e = record.get("stoptime");
					
					Date end = dateFormat.parse(e);
					t.addData(elementId, Semantic.TEMPORAL, new TemporalDuration(start.toInstant(), end.toInstant()));
					CSVRecord climateRecord = searchApproximatedClimateData(start, climates, refDates);
					t.addData(elementId, tempSemantic, Double.parseDouble(climateRecord.get("Temperature")));
					t.addData(elementId, windSemantic, Double.parseDouble(climateRecord.get("Wind Speed mph")));
					t.addData(elementId, weatherSemantic, Arrays.asList(Climate.parseClimates(climateRecord.get("Weather"))));
				} catch (ParseException e) {
					throw new IOException(e);
				}
				t.addData(elementId, USER, record.get("usertype"));
				t.addData(elementId, GENDER, record.get("gender"));
				t.addData(elementId, BIRTH_YEAR, record.get("birth year"));
				lastRecord = record;
				elementId++;
			}
			if(lastRecord != null) {
				double lat, lon;
				lat = Double.parseDouble(lastRecord.get("end station latitude"));
				lon = Double.parseDouble(lastRecord.get("end station longitude"));
				t.addData(elementId, Semantic.GEOGRAPHIC, new TPoint(lat, lon));
				try {
					Date start = dateFormat.parse(lastRecord.get("stoptime"));
					Date end = dateFormat.parse(lastRecord.get("stoptime"));
					end.setSeconds(end.getSeconds() + 1);
					t.addData(elementId, Semantic.TEMPORAL, new TemporalDuration(start.toInstant(), end.toInstant()));
					CSVRecord climateRecord = searchApproximatedClimateData(start, climates, refDates);
					t.addData(elementId, tempSemantic, Double.parseDouble(climateRecord.get("Temperature")));
					t.addData(elementId, windSemantic, Double.parseDouble(climateRecord.get("Wind Speed mph")));
					t.addData(elementId, weatherSemantic, Arrays.asList(Climate.parseClimates(climateRecord.get("Weather"))));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				t.addData(elementId, USER, lastRecord.get("usertype"));
				t.addData(elementId, GENDER, lastRecord.get("gender"));
				t.addData(elementId, BIRTH_YEAR, lastRecord.get("birth year"));
			}
			ret.add(t);
		}

		return ret;
	}
	
	private CSVRecord searchApproximatedClimateData(Date start, ConcurrentHashMap<Date,CSVRecord> climates, List<Date> dateKeys) {
		Date closest = searchClosestDate(start, dateKeys);
		return climates.get(closest);
	}

	private Date searchClosestDate(Date start, List<Date> dateKeys) {
		int indexEnd = dateKeys.size() - 1;
		if(indexEnd == 0) {
			return dateKeys.get(0);
		}
		Date middleTerm = dateKeys.get(indexEnd / 2);
		int compareTo = start.compareTo(middleTerm);
		if(compareTo == 0) {
			return start;
		} else if(compareTo > 0) {
			return searchClosestDate(start, dateKeys.subList(indexEnd / 2, indexEnd));
		}
		return searchClosestDate(start, dateKeys.subList(0, indexEnd / 2));
	}

	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		new BikeDataReader().read();
	}
}
