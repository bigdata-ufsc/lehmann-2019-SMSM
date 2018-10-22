import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.related.UMS;

public class UMSMaterialDidatico {

	public static void main(String[] args) {
		UMS ums = new UMS();
		computePQ(ums);
		computeRQ(ums);
		computeT1T2(ums);
	}

	private static void computeT1T2(UMS ums) {
		SemanticTrajectory T1 = new SemanticTrajectory(null, 2);
		T1.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(6,39));
		T1.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(21,39));
		T1.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(29,45));
		T1.addData(3, Semantic.SPATIAL_EUCLIDEAN, new TPoint(36,48));
		T1.addData(4, Semantic.SPATIAL_EUCLIDEAN, new TPoint(49,69));
		T1.addData(5, Semantic.SPATIAL_EUCLIDEAN, new TPoint(64,76));
		T1.addData(6, Semantic.SPATIAL_EUCLIDEAN, new TPoint(114,20));
		
		SemanticTrajectory T2 = new SemanticTrajectory(null, 2);
		T2.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(4,9));
		T2.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(16,18));
		T2.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(24,29));
		T2.addData(3, Semantic.SPATIAL_EUCLIDEAN, new TPoint(36,44));
		T2.addData(4, Semantic.SPATIAL_EUCLIDEAN, new TPoint(44,51));
		T2.addData(5, Semantic.SPATIAL_EUCLIDEAN, new TPoint(79,66));
		T2.addData(6, Semantic.SPATIAL_EUCLIDEAN, new TPoint(111,6));
		
		double similarity = ums.getSimilarity(T1, T2);
		System.out.println("UMS(T1, T2) = " + similarity);
	}

	private static void computePQ(UMS ums) {
		SemanticTrajectory Q = new SemanticTrajectory(null, 2);
		Q.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(40,10));
		Q.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(130,105));
		Q.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(205,185));
		
		SemanticTrajectory P = new SemanticTrajectory(null, 2);
		P.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(50,10));
		P.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(119,35));
		P.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(195,175));
		
		double similarity = ums.getSimilarity(Q, P);
		System.out.println("UMS(P, Q) = " + similarity);
	}

	private static void computeRQ(UMS ums) {
		SemanticTrajectory Q = new SemanticTrajectory(null, 2);
		Q.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(40,10));
		Q.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(130,105));
		Q.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(205,185));
		
		SemanticTrajectory R = new SemanticTrajectory(null, 2);
		R.addData(0, Semantic.SPATIAL_EUCLIDEAN, new TPoint(10,20));
		R.addData(1, Semantic.SPATIAL_EUCLIDEAN, new TPoint(110,120));
		R.addData(2, Semantic.SPATIAL_EUCLIDEAN, new TPoint(195,205));
		
		double similarity = ums.getSimilarity(Q, R);
		System.out.println("UMS(R, Q) = " + similarity);
	}
}
