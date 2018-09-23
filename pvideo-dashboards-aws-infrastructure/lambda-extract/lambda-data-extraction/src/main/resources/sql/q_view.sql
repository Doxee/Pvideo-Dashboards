select client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO as version,
       count(distinct event_id) as total, count(distinct shipment_id) as total_unique,  
	   min(load_date) as data_from, max(load_date) as data_to
from	   
	(select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO,
		 pe.id as event_id, 

		 case when pe2.load_date is null then sh.shipment_id else null end as shipment_id,
		 pe.load_date	 
		from SHIPMENT sh 
			 join (select id, shipment_id, doxee_platform_id, load_date from PVIDEO_EVENT where load_date between ? and ?
             and client_name = ? and procedure_name = ? 
			          and event_code='ACTION_PLAY'
					  and video_current_time=0  ) pe 
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
			 left join
				   (select shipment_id, doxee_platform_id, load_date from PVIDEO_EVENT where load_date < ?
                   and client_name = ? and procedure_name = ? 
				       and event_code='ACTION_PLAY'
					   and video_current_time=0 ) pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
		where sh.client_name = ?
		and sh.procedure_name = ?		
	) v
group by client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO;