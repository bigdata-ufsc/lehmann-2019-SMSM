package br.ufsc.lehmann.msm.artigo;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

/**
 * 
 * @author Andreas Thiele
 *
 *
 *         An implementation of knn. Uses Euclidean distance weighted by 1/distance
 * 
 *         Main method to classify if entry is male or female based on: Height, weight
 */
public class NearestNeighbour<T> {

	private int k;
	private List<Object> classes;
	private List<DataEntry<T>> dataSet;
	private IMeasureDistance<T> measurer;
	private boolean multithread;

	/**
	 * 
	 * @param dataSet
	 *            The set
	 * @param k
	 *            The number of neighbours to use
	 */
	public NearestNeighbour(List<DataEntry<T>> dataSet, int k, IMeasureDistance<T> measurer) {
		this(dataSet, k, measurer, false);
	}

	/**
	 * 
	 * @param dataSet
	 *            The set
	 * @param k
	 *            The number of neighbours to use
	 */
	public NearestNeighbour(List<DataEntry<T>> dataSet, int k, IMeasureDistance<T> measurer, boolean multithread) {
		this.measurer = measurer;
		this.multithread = multithread;
		this.classes = new ArrayList<Object>();
		this.k = k;
		this.dataSet = dataSet;

		// Load different classes
		for (DataEntry<T> entry : dataSet) {
			if (!classes.contains(entry.getY()))
				classes.add(entry.getY());
		}
	}

	private DataEntry<T>[] getNearestNeighbourType(DataEntry<T> x) {
		if (multithread) {
			try {
				return getNearestNeighbourTypeMultithreaded(x);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			return getNearestNeighbourTypeSinglethreaded(x);
		}
	}

	private DataEntry<T>[] getNearestNeighbourTypeSinglethreaded(DataEntry<T> x) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		DataEntry<T>[] retur = new DataEntry[this.k];
		double fjernest = Double.MIN_VALUE;
		int index = 0;
		for (DataEntry<T> tse : this.dataSet) {
			double distance = distance(x, tse);
			if (retur[retur.length - 1] == null) { // Hvis ikke fyldt
				int j = 0;
				while (j < retur.length) {
					if (retur[j] == null) {
						retur[j] = tse;
						break;
					}
					j++;
				}
				if (distance > fjernest) {
					index = j;
					fjernest = distance;
				}
			} else {
				if (distance < fjernest) {
					retur[index] = tse;
					double f = 0.0;
					int ind = 0;
					for (int j = 0; j < retur.length; j++) {
						double dt = distance(retur[j], x);
						if (dt > f) {
							f = dt;
							ind = j;
						}
					}
					fjernest = f;
					index = ind;
				}
			}
		}
		return retur;
	}

	private DataEntry<T>[] getNearestNeighbourTypeMultithreaded(DataEntry<T> x) throws InterruptedException {
		ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() / 8,
				Runtime.getRuntime().availableProcessors() / 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		List<DataEntry<T>> dataSet = new ArrayList<>(this.dataSet);
		DataEntry<T>[] retur = new DataEntry[this.k];
		DelayQueue<DelayedDistanceMeasure<T>> queueProcess = new DelayQueue<>();
		for (DataEntry<T> tse : dataSet) {
			Future<Double> future = executorService.submit(new Callable<Double>() {

				@Override
				public Double call() throws Exception {
					return distance(x, tse);
				}
			});
			queueProcess.add(new DelayedDistanceMeasure<T>(x, tse, future, 0));
		}
		while (!queueProcess.isEmpty()) {
			DelayedDistanceMeasure<T> toProcess = queueProcess.poll();
			if (toProcess == null) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			Future<Double> fut = toProcess.distance;
			if (!fut.isDone()) {
				queueProcess.add(new DelayedDistanceMeasure<T>(toProcess.a, toProcess.b, toProcess.distance, 50/* ms */));
			} else {
				double fjernest = Double.MIN_VALUE;
				int index = 0;
				Double distance;
				try {
					distance = fut.get();
					if (retur[retur.length - 1] == null) { // Hvis ikke fyldt
						int j = 0;
						while (j < retur.length) {
							if (retur[j] == null) {
								retur[j] = toProcess.b;
								break;
							}
							j++;
						}
						if (distance > fjernest) {
							index = j;
							fjernest = distance;
						}
					} else {
						if (distance < fjernest) {
							retur[index] = toProcess.b;
							double f = 0.0;
							int ind = 0;
							for (int j = 0; j < retur.length; j++) {
								double dt = distance(retur[j], x);
								if (dt > f) {
									f = dt;
									ind = j;
								}
							}
							fjernest = f;
							index = ind;
						}
					}
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
			}
		}
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.HOURS);
		return retur;
	}

	static class DelayedDistanceMeasure<T> implements Delayed {

		private DataEntry<T> a;
		private DataEntry<T> b;
		private Future<Double> distance;
		private long delay;

		DelayedDistanceMeasure(DataEntry<T> a, DataEntry<T> b, Future<Double> distance, int delay) {
			this.a = a;
			this.b = b;
			this.distance = distance;
			this.delay = TimeUnit.MILLISECONDS.toNanos(delay);
		}

		@Override
		public int compareTo(Delayed other) {
			if (other == this) // compare zero if same object
				return 0;
			if (other instanceof DelayedDistanceMeasure) {
				DelayedDistanceMeasure<?> x = (DelayedDistanceMeasure<?>) other;
				long diff = delay - x.delay;
				if (diff < 0)
					return -1;
				else if (diff > 0)
					return 1;
				else
					return 1;
			}
			long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
			return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(delay - System.nanoTime(), TimeUnit.NANOSECONDS);
		}

	}

	private double convertDistance(double d) {
		return 1.0 / d;
	}

	/**
	 * Computes distance
	 * 
	 * @param a
	 *            From
	 * @param b
	 *            To
	 * @return Distance
	 */
	public double distance(DataEntry<T> a, DataEntry<T> b) {
		return measurer.distance(a.getX(), b.getX());
	}

	/**
	 * 
	 * @param e
	 *            Entry to be classifies
	 * @return The class of the most probable class
	 */
	public Object classify(DataEntry<T> e) {
		HashMap<Object, Double> classcount = new HashMap<Object, Double>();
		DataEntry<T>[] de = this.getNearestNeighbourType(e);
		for (int i = 0; i < de.length; i++) {
			double distance = convertDistance(distance(de[i], e));
			if (!classcount.containsKey(de[i].getY())) {
				classcount.put(de[i].getY(), distance);
			} else {
				classcount.put(de[i].getY(), classcount.get(de[i].getY()) + distance);
			}
		}
		// Find right choice
		Object o = null;
		double max = 0;
		for (Object ob : classcount.keySet()) {
			if (classcount.get(ob) > max) {
				max = classcount.get(ob);
				o = ob;
			}
		}

		return o;
	}

	public static class DataEntry<T> {
		private T x;
		private Object y;

		public DataEntry(T x, Object y) {
			this.x = x;
			this.y = y;
		}

		public T getX() {
			return this.x;
		}

		public Object getY() {
			return this.y;
		}
	}
}