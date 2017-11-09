package br.ufsc.ftsm.evaluation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.ftsm.base.ETrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.method.FTSMBUBMSM;
import br.ufsc.ftsm.method.FTSMOMSM;
import br.ufsc.ftsm.method.FTSMQUBMSM;
import br.ufsc.ftsm.method.lcss.FTSMBLCSS;
import br.ufsc.ftsm.method.lcss.FTSMOLCSS;
import br.ufsc.ftsm.method.lcss.FTSMQLCSS;
import br.ufsc.ftsm.method.msm.FTSMBDMSM;
import br.ufsc.ftsm.method.msm.FTSMBMSM;
import br.ufsc.ftsm.method.msm.FTSMQDMSM;
import br.ufsc.ftsm.method.msm.FTSMQMSM;
import br.ufsc.ftsm.method.ums.FTSMBUMS;
import br.ufsc.ftsm.method.ums.FTSMBUMS3;
import br.ufsc.ftsm.related.DTW;
import br.ufsc.ftsm.related.DTW2;
import br.ufsc.ftsm.related.FTSELCSS;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSSL;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.PDTW;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.ftsm.related.UMS3;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.ftsm.util.CreateEllipseMath;



public class Scalability {
	private static DataSource source;
	private static DataRetriever retriever;

	public static void main(String[] args) throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		//Load data
		source = new DataSource("postgres", "postgres", "localhost", 5432,
				"postgis", DataSourceType.PGSQL, "uber_sanfrancisco", null, "geom");
		retriever = source.getRetriever();

		List<Trajectory> T = new ArrayList<Trajectory>();
		T = retriever.fastFetchTrajectories(); 

		//DTW-based measures
		DTW dtw = new DTW(Semantic.GEOGRAPHIC);
		DTW2 dtw2 = new DTW2();
		PDTW pdtw = new PDTW();
		
		//MSM-based measures
		MSM msm = new MSM(new MSMSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 100.0, 1));
		FTSMBMSM ftsmbmsm = new FTSMBMSM(100);
		FTSMBDMSM ftsmbdmsm = new FTSMBDMSM(100);

		FTSMBUBMSM ftsmbubmsm = new FTSMBUBMSM(100);
		FTSMQMSM ftsmqmsm = new FTSMQMSM(100);
		FTSMQDMSM ftsmqdmsm = new FTSMQDMSM(100);
		FTSMQUBMSM ftsmqubmsm = new FTSMQUBMSM(100);
		FTSMOMSM ftsmomsm = new FTSMOMSM(100);
		
		//LCSS-based measures
		LCSS lcss = new LCSS(new LCSSSemanticParameter<TPoint, Number>(Semantic.GEOGRAPHIC, 100.0));
		LCSSL lcssl = new LCSSL(100);
		FTSMBLCSS ftsmblcss = new FTSMBLCSS(100);
		FTSMQLCSS ftsmqlcss = new FTSMQLCSS(100);
		FTSMOLCSS ftsmolcss = new FTSMOLCSS(100);
		
		//UMS
		UMS ums = new UMS();
		//UMSMathSimple2 ums2 = new UMSMathSimple2();
		UMS3 ums3 = new UMS3();
		FTSMBUMS ftsmbums = new FTSMBUMS();
		FTSMBUMS3 ftsmbums2 = new FTSMBUMS3();
		
		FTSELCSS ftselcss = new FTSELCSS(100);
		
		int limit = 1867;
// 20% and full
//		Query result...
//		Creating ArrayList...
//		101082452	2179949	1917801	1988539	132717527	2761516	2718379	257352778	
//		Creating ellipses => 4420
//		266619557	
//		7043553	
//		Creating ellipses => 6109
//		3252957	
//		for (int k =10;k<=100;k+=10){
//			System.out.println(k);
//			Collections.shuffle(T);
//			showExecutionTimeLimitTopK(dtw2,T,k);
//			showExecutionTimeLimitTopK(dtw,T,k);
//			showExecutionTimeLimitETopK(ums3,T,k);
//			showExecutionTimeLimitETopK(ftsmbums2,T,k);
//		}
		
//		showExecutionTimeLimit(dtw2, T,limit);
//		showExecutionTimeLimit(dtw, T,limit);
		
		//20% e full ais 2009
//		Query result...
//		Creating ArrayList...
//		828420	104740	97701	73385	1055602	195011	161783	Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
		
		//showExecutionTimeLimit(dtw, T,limit);
		//showExecutionTimeLimit(pdtw, T,limit);
		
		//remover depois (antes de ver os resultados)
		//showExecutionTime(ftsmqlcss, T);
		//showExecutionTime(ftselcss, T);
		//
		
	//	showExecutionTimeLimit(msm, T,limit);
	//	showExecutionTimeLimit(ftsmbmsm, T,limit);
		//showExecutionTimeLimit(ftsmbubmsm, T,limit);
	//	showExecutionTimeLimit(ftsmbdmsm, T,limit);
	
		showExecutionTime(ftsmqmsm, T);
		showExecutionTime(ftselcss, T);
		showExecutionTime(msm, T);
//		showExecutionTimeLimit(ftsmqmsm, T,limit);
//		showExecutionTimeLimit(ftselcss, T,limit);
//		showExecutionTimeLimit(ftsmqdmsm, T,limit);
	//	showExecutionTimeLimit(ftsmqubmsm, T,limit);
		//77131703	63875951	53528408	54644832 crawdad50%
//		showExecutionTimeLimit(ftsmomsm, T,limit);
//		
//		showExecutionTimeLimit(lcss, T,limit);
		showExecutionTimeLimit(lcssl, T,limit);
		showExecutionTimeLimit(ftsmblcss, T,limit);
		showExecutionTimeLimit(ftsmqlcss, T,limit);
//		showExecutionTimeLimit(ftsmolcss, T,limit);
//		ais10%lcssl...653644	164803	138979
//		showExecutionTimeLimit(ftselcss, T,limit);
		//SF 3397827	2797294	2270128	2392185	2150209	4602572	2972123	2753937	

		showExecutionTimeLimit(ums, T,limit);
		showExecutionTimeLimitE3(ums3, T,limit);
		showExecutionTimeLimit(ftsmbums, T,limit);
		showExecutionTimeLimitE(ftsmbums2, T,limit);
		
		showExecutionTimeLimit(dtw2, T,limit);
		showExecutionTimeLimit(dtw, T,limit);
		
//		showExecutionTime(dtw, T);
//		showExecutionTime(pdtw, T);
//		
		showExecutionTime(msm, T);
		showExecutionTime(ftsmbmsm, T);
		showExecutionTime(ftsmbdmsm, T);
		showExecutionTime(ftsmqmsm, T);
//		
//		showExecutionTime(lcss, T);
		showExecutionTime(lcssl, T);
		showExecutionTime(ftsmblcss, T);
		showExecutionTime(ftsmqlcss, T);
//		showExecutionTime(ftsmolcss, T);
//		
//		showExecutionTime(ftselcss, T);
		
		System.out.println();
		

		


	}

	private static void showExecutionTime(TrajectorySimilarityCalculator tsc,
			List<Trajectory> T)
			throws SQLException {

		long start = System.currentTimeMillis();
		double result = 0.0;
		
		for (Trajectory t1 : T){
			for (Trajectory t2: T){
				result+=tsc.getSimilarity(t1, t2);
			}
		}
		
		long end = System.currentTimeMillis();

		System.out.print((end - start) + "	");
	}
	
	private static void showExecutionTimeLimit(TrajectorySimilarityCalculator tsc,
			List<Trajectory> T,int limit)
			throws SQLException {

		long start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<limit; i++){
			for (int j = 0; j<limit; j++){
				result+=tsc.getSimilarity(T.get(i), T.get(j));
			}
		}
		
		long end = System.currentTimeMillis();

		System.out.print((end - start) + "	");
	}
	
	private static void showExecutionTimeLimitE(FTSMBUMS3 tsc,
			List<Trajectory> T,int limit)
			throws SQLException {
		
		long start = System.currentTimeMillis();
		List<ETrajectory> E = new ArrayList<ETrajectory>();
		for (Trajectory t : T){
			E.add(new CreateEllipseMath().createEllipticalTrajectoryFixed(t));
			
		}
		
		long end = System.currentTimeMillis();
System.out.println();
		System.out.println("Creating ellipses => "+(end-start));
		start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<limit; i++){
			for (int j = 0; j<limit; j++){
				//System.out.println(T.get(i).getTid()+" : "+T.get(j).getTid());
				result+=tsc.getDistance(E.get(i), E.get(j));
			}
		}
		
		end = System.currentTimeMillis();

		System.out.println((end - start) + "	");
	}
	
	private static void showExecutionTimeLimitE3(UMS3 tsc,
			List<Trajectory> T,int limit)
			throws SQLException {
		
		long start = System.currentTimeMillis();
		List<ETrajectory> E = new ArrayList<ETrajectory>();
		for (Trajectory t : T){
			E.add(new CreateEllipseMath().createEllipticalTrajectoryFixed(t));
			
		}
		
		long end = System.currentTimeMillis();
System.out.println();
		System.out.println("Creating ellipses => "+(end-start));
		start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<limit; i++){
			for (int j = 0; j<limit; j++){
				//System.out.println(T.get(i).getTid()+" : "+T.get(j).getTid());
				result+=tsc.getDistance(E.get(i), E.get(j));
			}
		}
		
		end = System.currentTimeMillis();

		System.out.println((end - start) + "	");
	}

	
	private static void showExecutionTimeLimitTopK(TrajectorySimilarityCalculator tsc,
			List<Trajectory> T,int k)
			throws SQLException {
		
		long start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<k; i++){
			for (Trajectory T2 : T){
				//System.out.println(T.get(i).getTid()+" : "+T.get(j).getTid());
				result+=tsc.getSimilarity(T.get(i), T2);
			}
		}
		
		long end = System.currentTimeMillis();

		System.out.println((end - start) + "	result:"+result);
		
	}
	
	private static void showExecutionTimeLimitETopK(UMS3 tsc,
			List<Trajectory> T,int k)
			throws SQLException {
		
		long start = System.currentTimeMillis();
		List<ETrajectory> E = new ArrayList<ETrajectory>();
		for (Trajectory t : T){
			E.add(new CreateEllipseMath().createEllipticalTrajectoryFixed(t));
			
		}
		
		long end = System.currentTimeMillis();
//System.out.println();
	//	System.out.println("Creating ellipses => "+(end-start));
		start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<k; i++){
			for (ETrajectory E2 : E){
				//System.out.println(T.get(i).getTid()+" : "+T.get(j).getTid());
				result+=tsc.getDistance(E.get(i), E2);
			}
		}
		
		end = System.currentTimeMillis();

		System.out.println((end - start) + "	result:"+result);
		
	}
	
	private static void showExecutionTimeLimitETopK(FTSMBUMS3 tsc,
			List<Trajectory> T,int k)
			throws SQLException {
		
		long start = System.currentTimeMillis();
		List<ETrajectory> E = new ArrayList<ETrajectory>();
		for (Trajectory t : T){
			E.add(new CreateEllipseMath().createEllipticalTrajectoryFixed(t));
			
		}
		
		long end = System.currentTimeMillis();
//System.out.println();
	//	System.out.println("Creating ellipses => "+(end-start));
		start = System.currentTimeMillis();
		double result = 0.0;
		
		for (int i = 0; i<k; i++){
			for (ETrajectory E2 : E){
				//System.out.println(T.get(i).getTid()+" : "+T.get(j).getTid());
				result+=tsc.getDistance(E.get(i), E2);
			}
		}
		
		end = System.currentTimeMillis();

		System.out.println((end - start) + "	result:"+result);
		
	}
}
