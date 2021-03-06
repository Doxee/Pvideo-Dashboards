select client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO as version, event_code,
       count(distinct event_id) as total, count(distinct shipment_id) as total_unique,
       min(load_date) as data_from, max(load_date) as data_to
from
(select jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO,
		 pe.id as event_id, pe.load_date, pe.event_code,
		 --return shipment only if there aren't event in the past for the unique total value
		 case when pe2.load_date is null then sh.shipment_id else null end as shipment_id
		from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
			 join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id
			 join (select id, shipment_id, doxee_platform_id, load_date, event_code
			         from PVIDEO_EVENT where load_date > ?
			          and upper(substring(event_code from 1 for 5))='INTE' ) pe
on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
left join
(select shipment_id, doxee_platform_id, load_date, event_code
 from PVIDEO_EVENT where load_date <= ?
                         and upper(substring(event_code from 1 for 5))='INTE') pe2
on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
and pe.event_code=pe2.event_code
where jb.client_name = ?
and jb.procedure_name = ?
and jb.environment_name = ?
and jb.application_name = ?
)
group by client_name, procedure_name, environment_name, application_name, REGISTER_LETTER_NUMBER_TO, event_code

