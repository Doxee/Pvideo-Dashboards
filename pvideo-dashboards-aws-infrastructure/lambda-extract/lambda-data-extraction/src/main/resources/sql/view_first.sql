select client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO as version,
       date_hh, count(shipment_id) as total_unique, min(data_from) as data_from, max(data_to) as data_to
from
(
		select jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO,
		     datediff(hours, min(pu.end_date), min (pe.create_date)) as date_hh, pe.shipment_id, 	 
			 min(pe.load_date) as data_from, max(pe.load_date) as data_to
		from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
			 join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id
			 join (select shipment_id, doxee_platform_id, load_date, create_date 
			        from PVIDEO_EVENT where load_date > ?
			          and event_code='ACTION_PLAY'
					  and video_current_time=0 ) pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
			 join EMAIL_TRANSFER pu on pe.shipment_id=pu.shipment_id and pe.doxee_platform_id=pu.doxee_platform_id
			 left join
				   (select shipment_id, doxee_platform_id, load_date
				     from PVIDEO_EVENT where load_date <= ?
			          and event_code='ACTION_PLAY'
					  and video_current_time=0 ) pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
		where jb.client_name = ?
		and jb.procedure_name = ?
		and jb.environment_name = ?
		and jb.application_name = ?
		--tolgo non unique
		and pe2.load_date is null
		--
		group by jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO,
		     pe.shipment_id
	) 
group by client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO, date_hh
