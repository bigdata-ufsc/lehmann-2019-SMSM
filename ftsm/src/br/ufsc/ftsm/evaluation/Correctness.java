package br.ufsc.ftsm.evaluation;



import java.sql.SQLException;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.Trajectory;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.ftsm.method.FTSMOMSM;
import br.ufsc.ftsm.method.lcss.FTSMBLCSS;
import br.ufsc.ftsm.method.lcss.FTSMOLCSS;
import br.ufsc.ftsm.method.lcss.FTSMQLCSS;
import br.ufsc.ftsm.method.msm.FTSMBDMSM;
import br.ufsc.ftsm.method.msm.FTSMBMSM;
import br.ufsc.ftsm.method.msm.FTSMQMSM;
import br.ufsc.ftsm.method.ums.FTSMBUMS;
import br.ufsc.ftsm.method.ums.FTSMBUMS3;
import br.ufsc.ftsm.related.DTW;
import br.ufsc.ftsm.related.LCSS;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.ftsm.related.LCSSL;
import br.ufsc.ftsm.related.MSM;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.ftsm.related.PDTW;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.ftsm.related.UMS3;


public class Correctness {
	
	private static DataSource source;
	private static DataRetriever retriever;
	
	
public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	source = new DataSource("postgres", "postgres", "localhost", 5432,
			"postgis", DataSourceType.PGSQL, "amsterdan_park", null, "geom");
	retriever = source.getRetriever();
	
	retriever.prepareFetchTrajectoryStatement();
	Trajectory t1 = retriever.fetchTrajectory(97698,false); //amS
	Trajectory t2 = retriever.fetchTrajectory(91140,false);//ams

//	Trajectory t1 = retriever.fetchTrajectory(879,false); //trucks
//	Trajectory t2 = retriever.fetchTrajectory(880,false);//trucks

	//System.out.println("::: Baseline: "+(t1.length()*t2.length()));
	
	//DTW-based measures
	DTW dtw = new DTW(Semantic.SPATIAL);
	PDTW pdtw = new PDTW();
	
	//MSM-based measures
	MSM msm = new MSM(new MSMSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 100.0, 1));
	FTSMBMSM ftsmbmsm = new FTSMBMSM(100);
	FTSMBDMSM ftsmbdmsm = new FTSMBDMSM(100);
	FTSMQMSM ftsmqmsm = new FTSMQMSM(100);
	FTSMOMSM ftsmomsm = new FTSMOMSM(100);

	
	//LCSS-based measures
	LCSS lcss = new LCSS(new LCSSSemanticParameter<TPoint, Number>(Semantic.SPATIAL, 100.0));
	LCSSL lcssl = new LCSSL(100);
	FTSMBLCSS ftsmblcss = new FTSMBLCSS(100);
	FTSMQLCSS ftsmqlcss = new FTSMQLCSS(100);
	FTSMOLCSS ftsmolcss = new FTSMOLCSS(100);
	
	//UMS
	UMS ums = new UMS();
	UMS3 ums3 = new UMS3();
	FTSMBUMS ftsmbums = new FTSMBUMS();
	FTSMBUMS3 ftsmbums2 = new FTSMBUMS3();

	System.out.println("### DTW-Based:");
	System.out.println("DTW: "+dtw.getDistance(t1, t2));
	System.out.println("PDTW: "+pdtw.getSimilarity(t1, t2));
	
	System.out.println("### MSM-Based:");
	System.out.println("MSM: "+msm.getDistance(t1, t2));
	System.out.println("FTSMBMSM: "+ftsmbmsm.getSimilarity(t1, t2));
	System.out.println("FTSMBDMSM: "+ftsmbdmsm.getSimilarity(t1, t2));
	System.out.println("FTSMQMSM: "+ftsmqmsm.getSimilarity(t1, t2));
	System.out.println("FTSMOMSM: "+ftsmomsm.getSimilarity(t1, t2));
	
	System.out.println("### LCSS-Based:");
	System.out.println("LCSS: "+lcss.getDistance(t1, t2));
	System.out.println("LCSSL: "+lcssl.getSimilarity(t1, t2));
	System.out.println("FTSMBLCSS: "+ftsmblcss.getSimilarity(t1, t2));
	System.out.println("FTSMQLCSS: "+ftsmqlcss.getSimilarity(t1, t2));
	System.out.println("FTSMOLCSS: "+ftsmolcss.getSimilarity(t1, t2));
	
	
	System.out.println("### UMS-Based:");
	System.out.println("UMS: "+ums.getSimilarity(t1, t2));
	System.out.println("UMS3: "+ums3.getSimilarity(t1, t2));
	System.out.println("FTSMBUMS: "+ftsmbums.getSimilarity(t1, t2));
	System.out.println("FTSMBUMS3: "+ftsmbums2.getSimilarity(t1, t2));


	

}


}

