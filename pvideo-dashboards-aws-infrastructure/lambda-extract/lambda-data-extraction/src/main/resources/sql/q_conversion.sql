select client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO as version, video_element_id as event_code,
       count(distinct event_id) as total, count(distinct shipment_id) as total_unique,
	   min(load_date) as data_from, max(load_date) as data_to
from	   
	(select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO,
		 pe.id as event_id, pe.load_date, pe.video_element_id,

		 case when pe2.load_date is null then sh.shipment_id else null end as shipment_id		  
		from SHIPMENT sh 
			 join (select id, shipment_id, doxee_platform_id, load_date, video_element_id
        			 from PVIDEO_EVENT where load_date between ? and ?
					 and client_name = ? and procedure_name = ? 
			          and upper(substring(video_element_id from 1 for 4))='CONV' ) pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
			 left join
				   (select shipment_id, doxee_platform_id, load_date, video_element_id
    				   from PVIDEO_EVENT where load_date < ?
					   and client_name = ? and procedure_name = ?
				       and upper(substring(video_element_id from 1 for 4))='CONV') pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
				   and pe.video_element_id=pe2.video_element_id
		where sh.client_name = ?
		  and sh.procedure_name = ?	
	)
group by client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO, video_element_id;