SELECT id_colaborador_unidade, "unknown_POI", count("unknown_POI" not like '%2147483647')
	FROM involves."stops_FastCBSMoT_com_auditoria_weekly_200mts_30_mins"
    where "unknown_POI" is not null
    --and "unknown_POI" not like '%2147483647'
    group by id_colaborador_unidade, "unknown_POI"
    order by id_colaborador_unidade, "unknown_POI";

--Quantos colaboradores estão na base de trajs?
select count(distinct id_colaborador_unidade)
	from involves."dadoGps_com_auditoria" gps;

--Quantas trajs existem por colaborador?
select id_colaborador_unidade, count(distinct id_dimensao_data)
	from involves."dadoGps_com_auditoria" gps
    group by id_colaborador_unidade;


--Quantas semanas de trabalho possuem trajs por colaborador?
select id_colaborador_unidade, count(distinct dt.semana)
	from involves."dadoGps_com_auditoria" gps
    inner join involves.dimensao_data dt on gps.id_dimensao_data = dt.id
    group by id_colaborador_unidade;
    
--Quantas trajs existem por colaborador em cada semana que ele trabalhou?
select id_colaborador_unidade, dt.semana, count(distinct id_dimensao_data)
	from involves."dadoGps_com_auditoria" gps
    inner join involves.dimensao_data dt on gps.id_dimensao_data = dt.id
    group by id_colaborador_unidade, dt.semana;
    
--Quantas trajs existem por colaborador em cada dia da semana que ele trabalhou?
select id_colaborador_unidade, dt.dia_semana, count(distinct id_dimensao_data)
	from involves."dadoGps_com_auditoria" gps
    inner join involves.dimensao_data dt on gps.id_dimensao_data = dt.id
    group by id_colaborador_unidade, dt.dia_semana
    order by count(distinct id_dimensao_data) desc;

--Quantas paradas estavam:
--		(scheduled_aud_count) programadas
--		(checked_in_aud_count) check-in realizado
--		(total_stop_count) foram efetivamente feitas
--		(unknown_stop_count) são paradas em pontos desconhecidos
select gps.id_colaborador_unidade, gps.id_dimensao_data, 
count(distinct aud.id)  as scheduled_aud_count, 
count(distinct aud_realizada.id)  as checked_in_aud_count, 
count(distinct stop.id) as total_stop_count, 
count(distinct unknownStop.id)  as unknown_stop_count
	FROM involves."dadoGps_com_auditoria" gps
    inner join involves."stops_moves_FastCBSMoT_com_auditoria_200mts_30_mins" map 
    	on (gps.id_usuario::text || gps.id_dimensao_data::text || gps.id_dado_gps::text)::bigint = map.gps_point_id
    inner join involves."stops_FastCBSMoT_com_auditoria_200mts_30_mins" stop 
    	on stop.id = map.semantic_id
    left join involves."stops_FastCBSMoT_com_auditoria_200mts_30_mins" unknownStop 
    	on unknownStop.id = map.semantic_id and unknownStop."unknown_POI" is not null
    left join involves.auditoria aud
    	on aud.id_colaborador_unidade = gps.id_colaborador_unidade and aud.id_dimensao_data = gps.id_dimensao_data
    left join involves.auditoria aud_realizada
    	on aud_realizada.id = aud.id and aud_realizada."dt_entrada_check_in_manual" is not null
    group by gps.id_colaborador_unidade, gps.id_dimensao_data;
