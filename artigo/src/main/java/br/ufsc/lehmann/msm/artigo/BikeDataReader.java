package br.ufsc.lehmann.msm.artigo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
	

	private final class CSVComparator implements Comparator<CSVRecord> {
		private final Integer bikeid;
		private final DateFormat bIKE_FORMAT;

		private CSVComparator(Integer bikeid, DateFormat bIKE_FORMAT) {
			this.bikeid = bikeid;
			this.bIKE_FORMAT = bIKE_FORMAT;
		}

		public int compare(CSVRecord o1, CSVRecord o2) {
			String o1Start = o1.get("starttime");
			String o2Start = o2.get("starttime");
			try {
				return bIKE_FORMAT.parse(o1Start).compareTo(bIKE_FORMAT.parse(o2Start));
			} catch (ParseException | NumberFormatException e) {
				String o1end = o1.get("stoptime");
				String o2end = o2.get("stoptime");
				System.err.println("Bike - " + bikeid + ", dates - " + o1Start + " to " + o1end);
				System.err.println("Bike - " + bikeid + ", dates - " + o2Start + " to " + o2end);
				return -1;
			}
		}
	}

	public List<SemanticTrajectory> read() throws IOException, InterruptedException {
		CSVParser parser = CSVParser.parse(//
				new File("C:/Users/André/workspace/artigo/src/main/resources/Bike_Data/NYC/Bike-NYC.csv"), Charset.defaultCharset(),//
//				new File("C:/Users/André/workspace/artigo/src/main/resources/Bike_Data/NYC/teste.csv"), Charset.defaultCharset(),// 
				CSVFormat.EXCEL.withHeader("tripduration","starttime","stoptime","start station id","start station name","start station latitude","start station longitude","end station id","end station name","end station latitude","end station longitude","bikeid","usertype","birth year","gender"));
		// Get a list of CSV file records
		List<CSVRecord> csvRecords = parser.getRecords();

		// Read the CSV file records starting from the second record to skip the
		// header
		MultiValuedMap<Integer, CSVRecord> multiMap = new ArrayListValuedHashMap<Integer, CSVRecord>();

		for (int i = 1; i < csvRecords.size(); i++) {
			CSVRecord record = csvRecords.get(i);
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
//					final DateFormat BIKE_FORMAT = new SimpleDateFormat("MM/dd/YYYY HH:mm");
					final DateFormat BIKE_FORMAT = getFormat();
					System.out.println(((i / totalBikes) * 100) + "% de " + totalBikes);
					Collections.sort(records, new CSVComparator(bikeid, BIKE_FORMAT));
//					List<Object> orderedRecords = records.stream().sorted(new CSVCom;parator(bikeid, BIKE_FORMAT)).collect(Collectors.toList());
					String previousStation = null;
					int lastTrajectoryIdLocal = 0;
					for (CSVRecord record : records) {
//						CSVRecord record = (CSVRecord) rec;
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
		for (Integer trajectoryId : keySet) {
			Collection<CSVRecord> records = trajectories.get(trajectoryId);
			SemanticTrajectory t = new SemanticTrajectory(trajectoryId, 5);
			int elementId = 0;
			CSVRecord lastRecord = null;
			for (CSVRecord record : records) {
				double lat, lon;
				lat = Double.parseDouble(record.get("start station latitude"));
				lon = Double.parseDouble(record.get("start station longitude"));
				t.addData(elementId, Semantic.GEOGRAPHIC, new TPoint(lat, lon));
				try {
					t.addData(elementId, Semantic.TEMPORAL, new TemporalDuration(dateFormat.parse(record.get("starttime")).toInstant(), dateFormat.parse(record.get("stoptime")).toInstant()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				t.addData(elementId, new UserTypeSemantic(2), record.get("usertype"));
				t.addData(elementId, new GenderSemantic(3), record.get("gender"));
				t.addData(elementId, new BirthYearSemantic(4), record.get("birth year"));
				lastRecord = record;
				elementId++;
			}
			if(lastRecord != null) {
				double lat, lon;
				lat = Double.parseDouble(lastRecord.get("end station latitude"));
				lon = Double.parseDouble(lastRecord.get("end station longitude"));
				t.addData(elementId, Semantic.GEOGRAPHIC, new TPoint(lat, lon));
				try {
					t.addData(elementId, Semantic.TEMPORAL, new TemporalDuration(dateFormat.parse(lastRecord.get("starttime")).toInstant(), dateFormat.parse(lastRecord.get("stoptime")).toInstant()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				t.addData(elementId, new UserTypeSemantic(2), lastRecord.get("usertype"));
				t.addData(elementId, new GenderSemantic(3), lastRecord.get("gender"));
				t.addData(elementId, new BirthYearSemantic(4), lastRecord.get("birth year"));
			}
			ret.add(t);
		}

		return ret;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		new BikeDataReader().read();
	}
}
