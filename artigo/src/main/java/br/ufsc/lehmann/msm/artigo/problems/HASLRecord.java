package br.ufsc.lehmann.msm.artigo.problems;

public class HASLRecord {

	private int gid;
	private int tid;
	private String clazz;
	private String author;
	private double lx;
	private double ly;
	private double lz;
	private double lroll;
	private double lpitch;
	private double lyaw;
	private double lthumb;
	private double lfore;
	private double lmiddle;
	private double lring;
	private double llittle;
	private double rx;
	private double ry;
	private double rz;
	private double rroll;
	private double rpitch;
	private double ryaw;
	private double rthumb;
	private double rfore;
	private double rmiddle;
	private double rring;
	private double rlittle;
	
	public HASLRecord(int gid, int tid, String clazz, String author, double lx, double ly, double lz, double lroll,
			double lpitch, double lyaw, double lthumb, double lfore, double lmiddle, double lring, double llittle,
			double rx, double ry, double rz, double rroll, double rpitch, double ryaw, double rthumb, double rfore,
			double rmiddle, double rring, double rlittle) {
		this.gid = gid;
		this.tid = tid;
		this.clazz = clazz;
		this.author = author;
		this.lx = lx;
		this.ly = ly;
		this.lz = lz;
		this.lroll = lroll;
		this.lpitch = lpitch;
		this.lyaw = lyaw;
		this.lthumb = lthumb;
		this.lfore = lfore;
		this.lmiddle = lmiddle;
		this.lring = lring;
		this.llittle = llittle;
		this.rx = rx;
		this.ry = ry;
		this.rz = rz;
		this.rroll = rroll;
		this.rpitch = rpitch;
		this.ryaw = ryaw;
		this.rthumb = rthumb;
		this.rfore = rfore;
		this.rmiddle = rmiddle;
		this.rring = rring;
		this.rlittle = rlittle;
	}

	public int getTid(){
		return tid;
	}

	public void setTid(int tid){
		this.tid=tid;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getClazz(){
		return clazz;
	}

	public void setClazz(String clazz){
		this.clazz=clazz;
	}

	public double getLx(){
		return lx;
	}

	public void setLx(double lx){
		this.lx=lx;
	}

	public double getLy(){
		return ly;
	}

	public void setLy(double ly){
		this.ly=ly;
	}

	public double getLz(){
		return lz;
	}

	public void setLz(double lz){
		this.lz=lz;
	}

	public double getLroll(){
		return lroll;
	}

	public void setLroll(double lroll){
		this.lroll=lroll;
	}

	public double getLpitch(){
		return lpitch;
	}

	public void setLpitch(double lpitch){
		this.lpitch=lpitch;
	}

	public double getLyaw(){
		return lyaw;
	}

	public void setLyaw(double lyaw){
		this.lyaw=lyaw;
	}

	public double getLthumb(){
		return lthumb;
	}

	public void setLthumb(double lthumb){
		this.lthumb=lthumb;
	}

	public double getLfore(){
		return lfore;
	}

	public void setLfore(double lfore){
		this.lfore=lfore;
	}

	public double getLmiddle(){
		return lmiddle;
	}

	public void setLmiddle(double lmiddle){
		this.lmiddle=lmiddle;
	}

	public double getLring(){
		return lring;
	}

	public void setLring(double lring){
		this.lring=lring;
	}

	public double getLlittle(){
		return llittle;
	}

	public void setLlittle(double llittle){
		this.llittle=llittle;
	}

	public double getRx(){
		return rx;
	}

	public void setRx(double rx){
		this.rx=rx;
	}

	public double getRy(){
		return ry;
	}

	public void setRy(double ry){
		this.ry=ry;
	}

	public double getRz(){
		return rz;
	}

	public void setRz(double rz){
		this.rz=rz;
	}

	public double getRroll(){
		return rroll;
	}

	public void setRroll(double rroll){
		this.rroll=rroll;
	}

	public double getRpitch(){
		return rpitch;
	}

	public void setRpitch(double rpitch){
		this.rpitch=rpitch;
	}

	public double getRyaw(){
		return ryaw;
	}

	public void setRyaw(double ryaw){
		this.ryaw=ryaw;
	}

	public double getRthumb(){
		return rthumb;
	}

	public void setRthumb(double rthumb){
		this.rthumb=rthumb;
	}

	public double getRfore(){
		return rfore;
	}

	public void setRfore(double rfore){
		this.rfore=rfore;
	}

	public double getRmiddle(){
		return rmiddle;
	}

	public void setRmiddle(double rmiddle){
		this.rmiddle=rmiddle;
	}

	public double getRring(){
		return rring;
	}

	public void setRring(double rring){
		this.rring=rring;
	}

	public double getRlittle(){
		return rlittle;
	}

	public void setRlittle(double rlittle){
		this.rlittle=rlittle;
	}

	public String getAuthor(){
		return author;
	}

	public void setAuthor(String author){
		this.author=author;
	}

	public static class Hands {
		private Hand left;
		private Hand right;
		public Hands(Hand left, Hand right) {
			super();
			this.left = left;
			this.right = right;
		}
		public Hand getLeft() {
			return left;
		}
		public void setLeft(Hand left) {
			this.left = left;
		}
		public Hand getRight() {
			return right;
		}
		public void setRight(Hand right) {
			this.right = right;
		}
	}
	public static class Hand {
		private double x;
		private double y;
		private double z;
		private double roll;
		private double pitch;
		private double yaw;
		private double thumb;
		private double fore;
		private double middle;
		private double ring;
		private double little;
		
		
		public Hand(double x, double y, double z, double roll, double pitch, double yaw, double thumb, double fore,
				double middle, double ring, double little) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.roll = roll;
			this.pitch = pitch;
			this.yaw = yaw;
			this.thumb = thumb;
			this.fore = fore;
			this.middle = middle;
			this.ring = ring;
			this.little = little;
		}
		public double getX() {
			return x;
		}
		public void setX(double x) {
			this.x = x;
		}
		public double getY() {
			return y;
		}
		public void setY(double y) {
			this.y = y;
		}
		public double getZ() {
			return z;
		}
		public void setZ(double z) {
			this.z = z;
		}
		public double getRoll() {
			return roll;
		}
		public void setRoll(double roll) {
			this.roll = roll;
		}
		public double getPitch() {
			return pitch;
		}
		public void setPitch(double pitch) {
			this.pitch = pitch;
		}
		public double getYaw() {
			return yaw;
		}
		public void setYaw(double yaw) {
			this.yaw = yaw;
		}
		public double getThumb() {
			return thumb;
		}
		public void setThumb(double thumb) {
			this.thumb = thumb;
		}
		public double getFore() {
			return fore;
		}
		public void setFore(double fore) {
			this.fore = fore;
		}
		public double getMiddle() {
			return middle;
		}
		public void setMiddle(double middle) {
			this.middle = middle;
		}
		public double getRing() {
			return ring;
		}
		public void setRing(double ring) {
			this.ring = ring;
		}
		public double getLittle() {
			return little;
		}
		public void setLittle(double little) {
			this.little = little;
		}
		
	}
}