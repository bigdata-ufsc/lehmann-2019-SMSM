package br.ufsc.lehmann.msm.artigo;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

public class SemanticSelection {
	
	static float RANKER_THRESHOLD = 0.5f;

	public static void main(String[] args) {
		AttributeSelection attSelection = new AttributeSelection();

		attSelection.setEvaluator( new InfoGainAttributeEval() );

		attSelection.setRanking(true);

		Ranker rank = new Ranker();

		rank.setThreshold( RANKER_THRESHOLD );

		attSelection.setSearch(rank);

		attSelection.SelectAttributes( instancesTraining );

		instancesTraining = attSelection.reduceDimensionality( instancesTraining );
	}
}
