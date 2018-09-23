select client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO as version, PROGRESS,
       count(shipment_id) as total_unique, min(load_date) as data_from, max(load_date) as data_to
from (
	select jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO,
		  pe.shipment_id, pe.load_date,
          CASE
			WHEN pe.PROGRESS <= 10 THEN 10
			WHEN pe.PROGRESS > 10 and pe.PROGRESS <= 20 then 20
			WHEN pe.PROGRESS > 20 and pe.PROGRESS <= 30 then 30
			WHEN pe.PROGRESS > 30 and pe.PROGRESS <= 40 then 40
			WHEN pe.PROGRESS > 40 and pe.PROGRESS <= 50 then 50
			WHEN pe.PROGRESS > 50 and pe.PROGRESS <= 60 then 60
			WHEN pe.PROGRESS > 60 and pe.PROGRESS <= 70 then 70
			WHEN pe.PROGRESS > 70 and pe.PROGRESS <= 80 then 80
			WHEN pe.PROGRESS > 80 and pe.PROGRESS <= 90 then 90
			ELSE 100
			END as PROGRESS
		from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
			 join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id
			 join (select shipment_id, doxee_platform_id, load_date, PROGRESS
        			 from PVIDEO_EVENT where load_date > ?
			          and event_code='EVENT_PROGRESS_UPDATE') pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
			 left join
				   (select shipment_id, doxee_platform_id, load_date, PROGRESS
				      from PVIDEO_EVENT where load_date <= ?
				       and event_code='EVENT_PROGRESS_UPDATE') pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
				   and pe.PROGRESS= pe2.PROGRESS
		where jb.client_name = ?
		and jb.procedure_name = ?
        and jb.environment_name = ?
        and jb.application_name = ?
		and pe2.load_date is null)
group by client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO, PROGRESS
