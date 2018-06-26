package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Timestamp;

public class InvolvesRecord {

	private double lat;
	private double lon;
	private long id;
	private int id_usuario;
	private int id_dimensao_data;
	private Timestamp dt_coordenada;
	private Integer semanticMoveId;
	private Integer semanticStopId;
	private int semana;
	public int getId_colaborador_unidade() {
		return id_colaborador_unidade;
	}
	private int id_colaborador_unidade;
	public int getSemana() {
		return semana;
	}
	public InvolvesRecord(long id, int id_usuario, int id_colaborador_unidade, int id_dimensao_data, int semana, Timestamp dt_coordenada, double lat, double lon, Integer semanticStopId, Integer semanticMoveId) {
		super();
		this.id = id;
		this.id_usuario = id_usuario;
		this.id_colaborador_unidade = id_colaborador_unidade;
		this.id_dimensao_data = id_dimensao_data;
		this.semana = semana;
		this.dt_coordenada = dt_coordenada;
		this.lat = lat;
		this.lon = lon;
		this.semanticStopId = semanticStopId;
		this.semanticMoveId = semanticMoveId;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public long getId() {
		return id;
	}
	public int getId_usuario() {
		return id_usuario;
	}
	public int getId_dimensao_data() {
		return id_dimensao_data;
	}
	public Timestamp getDt_coordenada() {
		return dt_coordenada;
	}
	public Integer getSemanticStopId() {
		return semanticStopId;
	}
	public Integer getSemanticMoveId() {
		return semanticMoveId;
	}

}
