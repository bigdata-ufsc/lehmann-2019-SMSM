package br.ufsc.lehmann.classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.IClassificationExecutor;
import br.ufsc.lehmann.msm.artigo.IMeasureDistance;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;
import br.ufsc.lehmann.msm.artigo.Problem;

public class TestClassificationExecutor implements IClassificationExecutor {
	
	private Map<Object, DescriptiveStatistics> stats;

	public TestClassificationExecutor(Map<Object, DescriptiveStatistics> stats) {
		this.stats = stats;
	}

	@Override
	public void classify(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance) {
		List<SemanticTrajectory> training = problem.trainingData();
		List<SemanticTrajectory> testing = problem.testingData();
		List<SemanticTrajectory> validating = problem.validatingData();
		ArrayList<SemanticTrajectory> train = new ArrayList<>(training);
		train.addAll(testing);

		List<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		Semantic discriminator = problem.discriminator();
		for (SemanticTrajectory traj : train) {
			Object data = discriminator.getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(training.size(), 3), measureDistance,
				false);
		ExecutorService executorService = new ThreadPoolExecutor((int) (Runtime.getRuntime().availableProcessors() / 1.25),
				Runtime.getRuntime().availableProcessors(), 10L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
		DelayQueue<DelayedClassification> classifications = new DelayQueue<>();
		for (SemanticTrajectory semanticTrajectory : testing) {
			Future<Classification> submit = executorService.submit(new Callable<Classification>() {

				@Override
				public Classification call() throws Exception {
					return new Classification(semanticTrajectory, "Testing", nn.classify(new DataEntry<SemanticTrajectory>(semanticTrajectory, "descubra")));
				}
			});
			classifications.add(new DelayedClassification(submit, 0));
		}
		for (SemanticTrajectory semanticTrajectory : validating) {
			Future<Classification> submit = executorService.submit(new Callable<Classification>() {

				@Override
				public Classification call() throws Exception {
					return new Classification(semanticTrajectory, "Validating", nn.classify(new DataEntry<SemanticTrajectory>(semanticTrajectory, "descubra")));
				}
			});
			classifications.add(new DelayedClassification(submit, 0));
		}
		executorService.shutdown();
		while (!classifications.isEmpty()) {
			DelayedClassification toProcess = classifications.poll();
			if (toProcess == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (!toProcess.futureClassification.isDone()) {
				classifications.add(new DelayedClassification(toProcess.futureClassification, 1500/* ms */));
			} else {
				try {
					Classification classification = toProcess.futureClassification.get();
					SemanticTrajectory semanticTrajectory = classification.semanticTrajectory;
					Object classifiedAs = classification.classifiedAs;
					Object data = discriminator.getData(semanticTrajectory, 0);
					DescriptiveStatistics statistics = stats.get(data);
					if (data.equals(classifiedAs)) {
						statistics.addValue(1);
					} else {
						statistics.addValue(0);
					}
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class Classification {
		SemanticTrajectory semanticTrajectory;
		Object classifiedAs;
		private String phase;

		public Classification(SemanticTrajectory semanticTrajectory, String phase, Object classify) {
			this.semanticTrajectory = semanticTrajectory;
			this.phase = phase;
			classifiedAs = classify;
		}
	}

	static class DelayedClassification implements Delayed {

		private Future<Classification> futureClassification;
		private long delay;
		private long origin;

		DelayedClassification(Future<Classification> distance, int delay) {
			this.origin = System.currentTimeMillis();
			this.futureClassification = distance;
			this.delay = delay;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(delay - (System.currentTimeMillis() - origin), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed delayed) {
			if (delayed == this) {
				return 0;
			}

			if (delayed instanceof DelayedClassification) {
				long diff = delay - ((DelayedClassification) delayed).delay;
				return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
			}

			long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
			return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
		}
	}
}
