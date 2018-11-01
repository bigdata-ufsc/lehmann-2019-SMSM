package br.ufsc.lehmann.method;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.utils.Distance;
import br.ufsc.utils.EuclideanDistanceFunction;

public class EDwP extends TrajectorySimilarityCalculator<SemanticTrajectory> implements IMeasureDistance<SemanticTrajectory> {
	double totalLength;
	public Matrix matrix;
	double time;
	boolean computeTime;
	private SpatialDistanceFunction distFunc;

	public String getName() {
		return "EDwP";
	}

	public EDwP(SpatialDistanceFunction distFunc, boolean time) {
		this.distFunc = distFunc;
		this.computeTime = time;
	}

	public EDwP(boolean time) {
		this(new EuclideanDistanceFunction(), time);
	}

	public EDwP() {
		this(new EuclideanDistanceFunction(), true);
	}
	
	@Override
	public double distance(SemanticTrajectory t1, SemanticTrajectory t2) {
		return 1 - getSimilarity(t1, t2);
	}

	@Override
	public double getSimilarity(SemanticTrajectory s, SemanticTrajectory t) {
		TPoint[] t1Point = getAllPoints(s);
		TPoint[] t2Point = getAllPoints(t);
		double maxLength = Math.max(Distance.getDistance(t1Point, distFunc), Distance.getDistance(t2Point, distFunc));
		return 1 - getDistance(s, t) / maxLength;
	}

	public double getDistance(SemanticTrajectory s, SemanticTrajectory t) {
		TPoint[] t1Point = getAllPoints(s);
		TPoint[] t2Point = getAllPoints(t);

		STrajectory t1 = new STrajectory(0, -1, null, Arrays.asList(t1Point));
		STrajectory t2 = new STrajectory(0, -1, null, Arrays.asList(t2Point));
		this.matrix = new Matrix(t1.edges.size() + 1, t2.edges.size() + 1);

		initializeMatrix();

		this.totalLength = (Distance.getDistance(t1Point, distFunc) + Distance.getDistance(t2Point, distFunc));

		for (int i = 1; i < this.matrix.numRows(); i++) {
			for (int j = 1; j < this.matrix.numCols(); j++) {
				double rowDelta = Double.MAX_VALUE;
				double colDelta = Double.MAX_VALUE;
				double rowCoverage1 = Double.MAX_VALUE;
				double rowCoverage2 = Double.MAX_VALUE;
				double colCoverage1 = Double.MAX_VALUE;
				double colCoverage2 = Double.MAX_VALUE;
				double rowSpatialScore = Double.MAX_VALUE;
				double colSpatialScore = Double.MAX_VALUE;
				TPoint t2Insert = null;
				TPoint t1Insert = null;
				TPoint t2Edit = null;
				TPoint t1Edit = null;
				if (i > 1) {
					t1Edit = this.matrix.rowEdits[(i - 1)][j];
					t2Edit = this.matrix.colEdits[(i - 1)][j];
					double prevPointEdge = distFunc.distance(t1Edit, t1Point[i - 1]);

					t2Insert = lineMap((TPoint) t2Edit, t2Point[j], t1Point[i - 1]);
					double rowEditDistance = distFunc.distance(t2Insert, t1Point[i - 1]);
					double rowEditEdge = distFunc.distance(t2Edit, t2Insert);
					t2Insert.setTime(0.0D);
					rowCoverage1 = (rowEditEdge + prevPointEdge) / this.totalLength;
					rowCoverage2 = (distFunc.distance(t2Point[j], t2Insert) + t1.edgeLength(i - 1)) / this.totalLength;
					rowDelta = this.matrix.value[(i - 1)][j] - this.matrix.delta[(i - 1)][j]
							+ (rowEditDistance + distFunc.distance(t1Edit, t2Edit)) * rowCoverage1;
					rowSpatialScore = rowDelta
							+ (rowEditDistance + distFunc.distance(t2Point[j], t1Point[i])) * rowCoverage2;
				}
				if (j > 1) {
					t1Edit = this.matrix.rowEdits[i][(j - 1)];
					t2Edit = this.matrix.colEdits[i][(j - 1)];
					if (t1Edit == null) {
						break;
					}
					double prevPointEdge = distFunc.distance(t2Edit, t2Point[j - 1]);

					t1Insert = lineMap((TPoint) t1Edit, t1Point[i], t2Point[j - 1]);
					double colEditDistance = distFunc.distance(t1Insert, t2Point[j - 1]);
					double colEditEdge = distFunc.distance(t1Edit, t1Insert);

					t1Insert.setTime(0.0D);

					colCoverage1 = (colEditEdge + prevPointEdge) / this.totalLength;
					colCoverage2 = (distFunc.distance(t1Point[i], t1Insert) + t2.edgeLength(j - 1)) / this.totalLength;
					colDelta = this.matrix.value[i][(j - 1)] - this.matrix.delta[i][(j - 1)]
							+ (colEditDistance + distFunc.distance(t1Edit, t2Edit)) * colCoverage1;
					colSpatialScore = colDelta
							+ (colEditDistance + distFunc.distance(t1Point[i], t2Point[j])) * colCoverage2;
				}
				double diagCoverage = (t1.edgeLength(i - 1) + t2.edgeLength(j - 1)) / this.totalLength;

				double subScore = (distFunc.distance(t2Point[j], t1Point[i])
						+ distFunc.distance(t2Point[j - 1], t1Point[i - 1])) * diagCoverage;

				double diagScore = this.matrix.value[(i - 1)][(j - 1)] + subScore;
				if ((diagScore <= colSpatialScore) && (diagScore <= rowSpatialScore)) {
					this.matrix.add(i, j, diagScore, (byte) 1, t2Point[j - 1], t1Point[i - 1]);
					this.matrix.delta[i][j] = (diagScore - this.matrix.value[(i - 1)][(j - 1)]);
				} else if ((colSpatialScore < rowSpatialScore)
						|| ((colSpatialScore == rowSpatialScore) && (t2.edges.size() > t1.edges.size()))) {
					this.matrix.add(i, j, colSpatialScore, (byte) 2, t2Point[j - 1], t1Insert);
					this.matrix.delta[i][j] = (colSpatialScore - colDelta);
				} else {
					this.matrix.add(i, j, rowSpatialScore, (byte) 3, t2Insert, t1Point[i - 1]);
					this.matrix.delta[i][j] = (rowSpatialScore - rowDelta);
				}
			}
		}
		// double[] answer = { this.matrix.score(), this.time };
		return this.matrix.score();
	}

	private TPoint[] getAllPoints(SemanticTrajectory t1) {
		TPoint[] t1Point = new TPoint[t1.length()];
		for (int i = 0; i < t1.length(); i++) {
			t1Point[i] = Semantic.SPATIAL.getData(t1, i);
		}
		return t1Point;
	}

	public double getSubDistance(Box b, SemanticTrajectory st) {
		TPoint[] sPoints = getAllPoints(st);
		STrajectory t = new STrajectory(0, -1, null, Arrays.asList(sPoints));
		double d = 0.0D;
		TPoint s = ((Edge) t.edges.get(0)).p1;
		TPoint e = ((Edge) t.edges.get(t.edges.size() - 1)).p2;
		double recLength = Math.max(Math.abs(s.getX() - e.getX()), Math.abs(s.getY() - e.getY()));
		for (int i = 0; i < t.edges.size(); i++) {
			TPoint p1 = ((Edge) t.edges.get(i)).p1;
			TPoint p2 = ((Edge) t.edges.get(i)).p2;
			double d1 = b.euclidean(p1);
			double d2 = b.euclidean(p2);
			double minX = p1.getX() < p2.getX() ? p1.getX() : p2.getX();
			double maxX = p1.getX() > p2.getX() ? p1.getX() : p2.getX();
			double minY = p1.getY() < p2.getY() ? p1.getY() : p2.getY();
			double maxY = p1.getY() > p2.getY() ? p1.getY() : p2.getY();
			double recEdge = Math.min(maxX - minX, maxY - minY);

			Box b1 = new Box(minX, minY, maxX, maxY);
			if ((d1 == 0.0D) || (d2 == 0.0D) || (b1.euclidean(b) == 0.0D)) {
				d += (d1 + d2) * Math.min(t.edgeLength(i), recEdge)
						/ (Distance.getDistance(sPoints, distFunc) + recLength);
			} else {
				d += (d1 + d2) * t.edgeLength(i) / (Distance.getDistance(sPoints, distFunc) + recLength);
			}
		}
		return d > 0.0D ? d : 0.0D;
	}

	public TPoint intersect(TPoint p1, TPoint p2, TPoint p3, TPoint p4) {
		double d = (p1.getX() - p2.getX()) * (p3.getY() - p4.getY())
				- (p1.getY() - p2.getY()) * (p3.getX() - p4.getX());
		if (d == 0.0D) {
			return lineMap(p1, p2, p3);
		}
		double xi = ((p3.getX() - p4.getX()) * (p1.getX() * p2.getY() - p1.getY() * p2.getX())
				- (p1.getX() - p2.getX()) * (p3.getX() * p4.getY() - p3.getY() * p4.getX())) / d;
		double yi = ((p3.getY() - p4.getY()) * (p1.getX() * p2.getY() - p1.getY() * p2.getX())
				- (p1.getY() - p2.getY()) * (p3.getX() * p4.getY() - p3.getY() * p4.getX())) / d;
		return new TPoint(xi, yi, new Timestamp(-1));
	}

	private void initializeMatrix() {
		for (int i = 1; i < this.matrix.value.length; i++) {
			this.matrix.value[i][0] = Double.MAX_VALUE;
		}
		for (int j = 1; j < this.matrix.value[0].length; j++) {
			this.matrix.value[0][j] = Double.MAX_VALUE;
		}
		this.matrix.value[0][0] = 0.0D;
	}

	public void printPath() {
		this.matrix.printPath();
	}

	public TPoint lineMap(TPoint point1, TPoint point2, TPoint point) {
		double l2 = (point1.getX() - point2.getX()) * (point1.getX() - point2.getX())
				+ (point1.getY() - point2.getY()) * (point1.getY() - point2.getY());
		if (l2 == 0.0D) {
			return new TPoint(point.getX(), point.getY(), point.getTimestamp());
		}
		double t = ((point.getX() - point1.getX()) * (point2.getX() - point1.getX())
				+ (point.getY() - point1.getY()) * (point2.getY() - point1.getY())) / l2;
		if (t < 0.0D) {
			return new TPoint(point1.getX(), point1.getY(), point1.getTimestamp());
		}
		if (t > 1.0D) {
			return new TPoint(point2.getX(), point2.getY(), point2.getTimestamp());
		}
		return new TPoint(point1.getX() + t * (point2.getX() - point1.getX()),
				point1.getY() + t * (point2.getY() - point1.getY()), point1.getTimestamp());
	}
	
	@Override
	public String name() {
		return "EDwP";
	}

	public static class Matrix {

		double[][] value;
		double[][] delta;
		public byte[][] parent;
		public TPoint[][] colEdits;
		public TPoint[][] rowEdits;

		public Matrix(int rowNum, int colNum) {
			this.value = new double[rowNum][colNum];
			this.delta = new double[rowNum][colNum];
			this.parent = new byte[rowNum][colNum];
			this.colEdits = new TPoint[rowNum][colNum];
			this.rowEdits = new TPoint[rowNum][colNum];
		}

		public void add(int i, int j, double val, byte parent, TPoint colEdit, TPoint rowEdit) {
			this.value[i][j] = val;
			this.parent[i][j] = parent;
			this.colEdits[i][j] = colEdit;
			this.rowEdits[i][j] = rowEdit;
		}

		public int numRows() {
			return this.value.length;
		}

		public int numCols() {
			return this.value[0].length;
		}

		public double score() {
			return this.value[(this.value.length - 1)][(this.value[0].length - 1)];
		}

		public void printPath() {
			int i = numRows() - 1;
			int j = numCols() - 1;
			System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
			if (this.parent[i][j] == 1) {
				i--;
				j--;
			} else if (this.parent[i][j] == 2) {
				j--;
			} else {
				i--;
			}
			printPath(i, j);
		}

		public void getSubMatchLength() {
			int j = -1;
			double min = Double.MAX_VALUE;
			for (int i = 1; i < this.value.length; i++) {
				if (this.value[i][(this.value[0].length - 1)] < min) {
					min = this.value[i][(this.value[0].length - 1)];
					j = i;
				}
			}
			int i = numRows() - 1;
			System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
			if (this.parent[i][j] == 1) {
				i--;
				j--;
			} else if (this.parent[i][j] == 2) {
				j--;
			} else {
				i--;
			}
			printPath(i, j);
		}

		private void printPath(int i, int j) {
			System.out.println(i + " " + j + " " + this.value[i][j] + " " + this.delta[i][j]);
			if ((i == 0) || (j == 0)) {
				return;
			}
			if (this.parent[i][j] == 1) {
				i--;
				j--;
			} else if (this.parent[i][j] == 2) {
				j--;
			} else {
				i--;
			}
			printPath(i, j);
		}

		public double subScore() {
			double min = Double.MAX_VALUE;
			for (int i = 1; i < this.value.length; i++) {
				if (this.value[i][(this.value[0].length - 1)] < min) {
					min = this.value[i][(this.value[0].length - 1)];
				}
			}
			return min;
		}

		public void print() {
			System.out.println("=======================");
			for (int i = 0; i < this.value.length; i++) {
				System.out.println(Arrays.toString(this.value[i]));
			}
			System.out.println("=======================");
		}

		public int getSubPoint() {
			double min = Double.MAX_VALUE;
			int minI = -1;
			for (int i = 1; i < this.value.length; i++) {
				if (this.value[i][(this.value[0].length - 1)] < min) {
					min = this.value[i][(this.value[0].length - 1)];
					minI = i;
				}
			}
			return minI;
		}
	}

	public class STrajectory {
		public ArrayList<Edge> edges;
		public int index;
		public int trajID;
		public Timestamp startDate;
		public double length;

		public STrajectory(int index, int trajID, Timestamp startDate, List<TPoint> points) {
			this.edges = new ArrayList<>();
			this.index = index;
			this.trajID = trajID;
			this.startDate = startDate;
			this.length = 0.0D;
			for (int i = 0; i < points.size() - 1; i++) {
				TPoint p1 = (TPoint) points.get(i);
				TPoint p2 = (TPoint) points.get(i + 1);
				this.edges.add(new Edge(p1, p2));
				this.length += ((Edge) this.edges.get(i)).length;
			}
		}

		public String toString() {
			String str = this.index + " " + this.trajID + " " + this.startDate + " [";
			for (int i = 0; i < this.edges.size() + 1; i++) {
				str = str + getPoint(i).toString() + ";";
			}
			return str + "]";
		}

		public Edge getEdge(int i) {
			return (Edge) this.edges.get(i);
		}

		public double edgeLength(int i) {
			return ((Edge) this.edges.get(i)).length;
		}

		public String getLabel() {
			return this.index + " " + this.trajID;
		}

		public TPoint getPoint(int j) {
			if (j == 0) {
				return ((Edge) this.edges.get(0)).p1;
			}
			return ((Edge) this.edges.get(j - 1)).p2;
		}
	}

	public class Edge {
		public double length;
		public TPoint p1;
		public TPoint p2;
		public double speed;

		public Edge(TPoint x, TPoint y) {
			this(x, y, new EuclideanDistanceFunction());
		}

		public Edge(TPoint x, TPoint y, SpatialDistanceFunction distFunc) {
			this.p1 = x;
			this.p2 = y;
			this.length = distFunc.distance(this.p1, this.p2);
			if (this.length == 0.0D) {
				this.speed = 0.0D;
			} else {
				this.speed = (this.length / (this.p2.getTime() - this.p1.getTime()));
			}
			if (this.speed < 0.0D) {
				this.speed = 0.0D;
			}
		}

		public String toString() {
			return this.p1.toString() + ";" + this.p2.toString() + ";" + "Speed: " + this.speed;
		}
	}

	public class BoxEdge {
		public double length;
		public Box p1;
		public Box p2;

		public BoxEdge(Box x, Box y) {
			this.p1 = x;
			this.p2 = y;
			this.length = this.p1.euclidean(this.p2);
		}

		public double getArea() {
			if (this.p1.area() == 0.0D) {
				return this.p2.area();
			}
			if (this.p2.area() == 0.0D) {
				return this.p1.area();
			}
			TPoint s1;
			TPoint s2;
			if (this.p1.x1 < this.p2.x1) {
				if (this.p1.y1 < this.p2.y1) {
					s2 = this.p2.p2();

					double base = this.p2.y2 - this.p1.y2;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s1 = new TPoint(this.p1.x1 - this.p1.height * (this.p2.x1 - this.p1.x1) / base, this.p1.y1);
				} else {
					double base = this.p2.y1 - this.p1.y1;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s2 = new TPoint(this.p1.x1 - this.p1.height * (this.p2.x1 - this.p1.x1) / base, this.p1.y2);
					s1 = this.p2.p1();
				}
			} else {
				if (this.p1.y1 < this.p2.y1) {
					double base = this.p2.y1 - this.p1.y1;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s2 = new TPoint(this.p2.x1 - this.p2.height * (this.p2.x1 - this.p1.x1) / base, this.p2.y2);
					s1 = this.p1.p1();
				} else {
					s2 = this.p1.p2();
					double base = this.p2.y2 - this.p1.y2;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s1 = new TPoint(this.p2.x1 - this.p2.height * (this.p2.x1 - this.p1.x1) / base, this.p2.y2);
				}
			}
			TPoint s4;

			TPoint s3;
			if (this.p1.x2 > this.p2.x2) {

				if (this.p1.y1 < this.p2.y1) {
					s4 = this.p2.p4();
					double base = this.p1.y2 - this.p2.y2;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s3 = new TPoint(this.p1.x2 + this.p1.height * (this.p1.x2 - this.p2.x2) / base, this.p1.y1);
				} else {
					s3 = this.p2.p3();
					double base = this.p1.y1 - this.p2.y1;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s4 = new TPoint(this.p1.x2 + this.p1.height * (this.p1.x2 - this.p2.x2) / base, this.p1.y2);
				}
			} else {
				if (this.p1.y1 < this.p2.y1) {
					s3 = this.p1.p3();
					double base = this.p2.y1 - this.p1.y1;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s4 = new TPoint(this.p2.x2 + this.p2.height * (this.p2.x2 - this.p1.x2) / base, this.p2.y2);
				} else {
					s4 = this.p1.p4();
					double base = this.p1.y2 - this.p2.y2;
					if (base == 0.0D) {
						base = 1.0D;
					}
					s3 = new TPoint(this.p2.x2 + this.p2.height * (this.p1.x2 - this.p2.x2) / base, this.p2.y1);
				}
			}
			return trapArea(s1, s2, s3, s4) / 2.0D;
		}

		private double trapArea(TPoint s1, TPoint s2, TPoint s3, TPoint s4) {
			return 0.5D * (Math.abs(s3.getX() - s1.getX()) + Math.abs(s4.getX() - s2.getX()))
					* Math.abs(s2.getY() - s1.getY());
		}
	}

	public class Box {
		public double x1;
		public double y1;
		public double x2;
		public double y2;
		public double width;
		public double height;

		public Box(TPoint p1) {
			this.x1 = p1.getX();
			this.y1 = p1.getY();
			this.x2 = this.x1;
			this.y2 = this.y1;
			this.width = 0.0D;
			this.height = 0.0D;
		}

		public Box() {
			this.x2 = -2.147483648E9D;
			this.y2 = this.x2;
			this.x1 = 2.147483647E9D;
			this.y1 = this.x1;
		}

		public Box(ArrayList<STrajectory> db) {
			double maxX = -2.147483648E9D;
			double maxY = maxX;
			double minX = 2.147483647E9D;
			double minY = minX;
			// Trajectory t;
			for (STrajectory t : db) {
				int i = 0;

				while (i < t.edges.size() + 1) {

					TPoint p = t.getPoint(i);
					if (p.getX() > maxX) {
						maxX = p.getX();
					}
					if (p.getY() > maxY) {
						maxY = p.getY();
					}
					if (p.getX() < minX) {
						minX = p.getX();
					}
					if (p.getY() < minY) {
						minY = p.getY();
					}
					i++;
				}
			}
			this.x1 = minX;
			this.y1 = minY;
			this.x2 = maxX;
			this.y2 = maxY;
			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public String toString() {
			return "(" + this.x1 + " " + this.y1 + ")" + "(" + this.x2 + " " + this.y2 + ")";
		}

		public Box(double minx, double miny, double maxx, double maxy) {
			this.x1 = minx;
			this.y1 = miny;
			this.x2 = maxx;
			this.y2 = maxy;
			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public Box(Box b, TPoint p) {
			this.x1 = (p.getX() < b.x1 ? p.getX() : b.x1);
			this.y1 = (p.getY() < b.y1 ? p.getY() : b.y1);
			this.x2 = (p.getX() > b.x2 ? p.getX() : b.x2);
			this.y2 = (p.getY() > b.y2 ? p.getY() : b.y2);

			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public Box(Box b) {
			this.x1 = b.x1;
			this.x2 = b.x2;
			this.y1 = b.y1;
			this.y2 = b.y2;
			this.width = b.width;
			this.height = b.height;
		}

		public Box(Box b1, Box b2) {
			this.x1 = (b1.x1 < b2.x1 ? b1.x1 : b2.x1);
			this.y1 = (b1.y1 < b2.y1 ? b1.y1 : b2.y1);
			this.x2 = (b1.x2 > b2.x2 ? b1.x2 : b2.x2);
			this.y2 = (b1.y2 > b2.y2 ? b1.y2 : b2.y2);

			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public Box(TPoint b1, TPoint b2) {
			this.x1 = (b1.getX() < b2.getX() ? b1.getX() : b2.getX());
			this.y1 = (b1.getY() < b2.getY() ? b1.getY() : b2.getY());
			this.x2 = (b1.getX() > b2.getX() ? b1.getX() : b2.getX());
			this.y2 = (b1.getY() > b2.getY() ? b1.getY() : b2.getY());
			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public boolean equals(Box b) {
			return (this.x1 == b.x1) && (this.x2 == b.x2) && (this.y1 == b.y1) && (this.y2 == b.y2);
		}

		public double euclidean(Box p) {
			double dx = (this.x1 > p.x1 ? this.x1 : p.x1) - (this.x2 < p.x2 ? this.x2 : p.x2);
			double dy = (this.y1 > p.y1 ? this.y1 : p.y1) - (this.y2 < p.y2 ? this.y2 : p.y2);
			if (dx < 0.0D) {
				return dy > 0.0D ? dy : 0.0D;
			}
			if (dy < 0.0D) {
				return dx;
			}
			return Math.sqrt(dx * dx + dy * dy);
		}

		public double euclidean(TPoint p) {
			if (inBox(p)) {
				return 0.0D;
			}
			double dx = (this.x1 > p.getX() ? this.x1 : p.getX()) - (this.x2 < p.getX() ? this.x2 : p.getX());
			double dy = (this.y1 > p.getY() ? this.y1 : p.getY()) - (this.y2 < p.getY() ? this.y2 : p.getY());
			if (dx < 0.0D) {
				return dy > 0.0D ? dy : 0.0D;
			}
			if (dy < 0.0D) {
				return dx;
			}
			return Math.sqrt(dx * dx + dy * dy);
		}

		boolean inBox(TPoint p) {
			return (p.getX() >= this.x1) && (p.getX() <= this.x2) && (p.getY() >= this.y1) && (p.getY() <= this.y2);
		}

		public TPoint p4() {
			return new TPoint(this.x2, this.y2);
		}

		public TPoint p3() {
			return new TPoint(this.x2, this.y1);
		}

		public TPoint p2() {
			return new TPoint(this.x1, this.y2);
		}

		public TPoint p1() {
			return new TPoint(this.x1, this.y1);
		}

		public double area() {
			return this.width * this.height;
		}

		public void join(TPoint p) {
			this.x1 = (p.getX() < this.x1 ? p.getX() : this.x1);
			this.y1 = (p.getY() < this.y1 ? p.getY() : this.y1);
			this.x2 = (p.getX() > this.x2 ? p.getX() : this.x2);
			this.y2 = (p.getY() > this.y2 ? p.getY() : this.y2);
			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public void join(STrajectory t) {
			for (int i = 0; i < t.edges.size() + 1; i++) {
				TPoint p = t.getPoint(i);
				if (p.getX() >= this.x2) {
					this.x2 = p.getX();
				}
				if (p.getY() >= this.y2) {
					this.y2 = p.getY();
				}
				if (p.getX() < this.x1) {
					this.x1 = p.getX();
				}
				if (p.getY() < this.y1) {
					this.y1 = p.getY();
				}
			}
			this.width = (this.x2 - this.x1);
			this.height = (this.y2 - this.y1);
		}

		public TPoint samplePoint() {
			Random r = new Random();
			if (this.width == 0.0D) {
				this.width = 1.0D;
			}
			if (this.height == 0.0D) {
				this.height = 1.0D;
			}
			return new TPoint(r.nextInt((int) this.width) + this.x1, r.nextInt((int) this.height) + this.y1);
		}
	}

}
