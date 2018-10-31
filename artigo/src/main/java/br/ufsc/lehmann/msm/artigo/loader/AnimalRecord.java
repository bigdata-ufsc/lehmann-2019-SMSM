package br.ufsc.lehmann.msm.artigo.loader;

import java.sql.Timestamp;
import java.util.Date;

import br.ufsc.core.trajectory.TPoint;

public class AnimalRecord {

	private int gid;
	private String tid;
	private TPoint latlon;
	private Timestamp timestamp;
	private String specie;
	private Date grensunr;
	private Date grensuns;
	private double obswt;
	private long starkeyTime;

	public AnimalRecord(int gid, String tid, TPoint latlon, Timestamp timestamp, String specie, Date grensunr, Date grensuns,
			double obswt, long starkeyTime) {
				this.gid = gid;
				this.tid = tid;
				this.latlon = latlon;
				this.timestamp = timestamp;
				this.specie = specie;
				this.grensunr = grensunr;
				this.grensuns = grensuns;
				this.obswt = obswt;
				this.starkeyTime = starkeyTime;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public TPoint getLatlon() {
		return latlon;
	}

	public void setLatlon(TPoint latlon) {
		this.latlon = latlon;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getSpecie() {
		return specie;
	}

	public void setSpecie(String specie) {
		this.specie = specie;
	}

	public Date getGrensunr() {
		return grensunr;
	}

	public void setGrensunr(Date grensunr) {
		this.grensunr = grensunr;
	}

	public Date getGrensuns() {
		return grensuns;
	}

	public void setGrensuns(Date grensuns) {
		this.grensuns = grensuns;
	}

	public double getObswt() {
		return obswt;
	}

	public void setObswt(double obswt) {
		this.obswt = obswt;
	}

	public long getStarkeyTime() {
		return starkeyTime;
	}

	public void setStarkeyTime(long starkeyTime) {
		this.starkeyTime = starkeyTime;
	}
}
