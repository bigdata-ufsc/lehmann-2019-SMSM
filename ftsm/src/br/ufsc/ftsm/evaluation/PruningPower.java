package br.ufsc.ftsm.evaluation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.ftsm.method.FTSMBPMSM;
import br.ufsc.ftsm.method.FTSMOMSM;
import br.ufsc.ftsm.method.euclidean.FTSMBDMSM;
import br.ufsc.ftsm.method.lcss.FTSMBLCSS;
import br.ufsc.ftsm.method.lcss.FTSMOLCSS;
import br.ufsc.ftsm.method.lcss.FTSMQLCSS;
import br.ufsc.ftsm.method.msm.FTSMBMSM;
import br.ufsc.ftsm.method.msm.FTSMQMSM;
import br.ufsc.ftsm.related.DTW;
import br.ufsc.ftsm.related.FTSELCSS;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSSL;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.PDTW;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;



public class PruningPower {
	private static DataSource source;
	private static DataRetriever retriever;

	public static void main(String[] args) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		//Load data
		source = new DataSource("postgres", "postgres", "localhost", 5432,
				"postgis", DataSourceType.PGSQL, "amsterdan_park", null, "geom");
		retriever = source.getRetriever();

		List<Trajectory> T = new ArrayList<Trajectory>();
		T = retriever.fastFetchTrajectories(); 

		//DTW-based measures
		DTW dtw = new DTW(Semantic.SPATIAL);
		PDTW pdtw = new PDTW();
		
		//MSM-based measures
		MSM msm = new MSM(new MSMSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 100.0, 1));
		FTSMBMSM ftsmbmsm = new FTSMBMSM(100);
		FTSMBDMSM ftsmbdmsm = new FTSMBDMSM(100);

		FTSMBPMSM ftsmbpmsm = new FTSMBPMSM(100);
		FTSMQMSM ftsmqmsm = new FTSMQMSM(100);
		FTSMOMSM ftsmomsm = new FTSMOMSM(100);
		
		//LCSS-based measures
		LCSS lcss = new LCSS(new LCSSSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 100.0));
		LCSSL lcssl = new LCSSL(100);
		FTSMBLCSS ftsmblcss = new FTSMBLCSS(100);
		FTSMQLCSS ftsmqlcss = new FTSMQLCSS(100);
		FTSMOLCSS ftsmolcss = new FTSMOLCSS(100);
		
		FTSELCSS ftselcss = new FTSELCSS(100);
		
		int limit = 1861;
//
		//showExecutionTimeLimit(dtw, T,limit);
		//showExecutionTimeLimit(pdtw, T,limit);
		
//		showExecutionTimeLimit(msm, T,limit);
//		showExecutionTimeLimit(ftsmbmsm, T,limit);
//		showExecutionTimeLimit(ftsmbdmsm, T,limit);
//		showExecutionTimeLimit(ftsmqmsm, T,limit);
		//77131703	63875951	53528408	54644832 crawdad50%
//		showExecutionTimeLimit(ftsmomsm, T,limit);
//		
//		showExecutionTimeLimit(lcss, T,limit);
//		showExecutionTimeLimit(lcssl, T,limit);
//		showExecutionTimeLimit(ftsmblcss, T,limit);
//		showExecutionTimeLimit(ftsmqlcss, T,limit);
//		showExecutionTimeLimit(ftsmolcss, T,limit);
//		ais10%lcssl...653644	164803	138979
//		showExecutionTimeLimit(ftselcss, T,limit);
		
//		showExecutionTime(dtw, T);
//		showExecutionTime(pdtw, T);
//		
//		showExecutionTime(msm, T);
		
//		for (Trajectory t1 : T){
//			for (Trajectory t2: T){
//				if (ftsmbdmsm.getDistance(t1, t2)!=ftsmbmsm.getDistance(t1, t2)){
//					System.out.println(t1.getTid()+" "+t2.getTid());
//				}
//			}
//		}
		
		System.out.println("end");
		
		long euclidean=0;
		for (Trajectory t1 : T){
			for (Trajectory t2: T){
				euclidean+=ftselcss.getEuclidean(t1, t2);
			}
		}//699747320 694204526
		System.out.println(euclidean);
//		showExecutionTime(ftsmqmsm, T);
//		
//		showExecutionTime(lcss, T);
//		showExecutionTime(lcssl, T);
//		showExecutionTime(ftsmblcss, T);
//		showExecutionTime(ftsmqlcss, T);
//		showExecutionTime(ftsmolcss, T);
//		
//		showExecutionTime(ftselcss, T);
		
		System.out.println();
		

		


	}


}
