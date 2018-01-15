package br.ufsc.lehmann.method;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisDataReader;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeProblem;
import br.ufsc.utils.EuclideanDistanceFunction;

public interface wDFTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		if(problem instanceof NElementProblem) {
			return new wDF(3, NElementProblem.DISTANCE_FUNCTION);
		} else if(problem instanceof PatelProblem) {
			return new wDF(3, PatelDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof VehicleProblem) {
			return new wDF(3, PatelDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof NewYorkBusProblem) {
			return new wDF(3, NewYorkBusDataReader.GEO_DISTANCE_FUNCTION);
		} else if(problem instanceof DublinBusProblem) {
			return new wDF(3, DublinBusDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof GeolifeProblem) {
			return new wDF(3, GeolifeDataReader.GEO_DISTANCE_FUNCTION);
		} else if(problem instanceof SanFranciscoCabProblem) {
			return new wDF(3, SanFranciscoCabDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof SergipeTracksProblem) {
			return new wDF(3, SergipeTracksDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof PrototypeProblem) {
			return new wDF(3, new EuclideanDistanceFunction());
		} else if(problem instanceof PisaProblem) {
			return new wDF(3, PisaDataReader.DISTANCE_FUNCTION);
		} else if(problem instanceof HermoupolisProblem) {
			return new wDF(3, HermoupolisDataReader.DISTANCE_FUNCTION);
		}
		return null;}
}
