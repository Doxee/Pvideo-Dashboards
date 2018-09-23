select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO as version,
       pu.user_agent, count(distinct pe.shipment_id) as total_unique,
	   min(pe.load_date) as data_from, max(pe.load_date) as data_to
from SHIPMENT sh 
	 join (select id, shipment_id, doxee_platform_id, load_date 
			from PVIDEO_EVENT 
			where load_date between ? and ?
            and client_name = ? and procedure_name = ? 
			  and event_code = 'ACTION_PLAY'
			  and video_current_time=0  ) pe
		  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
	 left join
		   (select shipment_id, doxee_platform_id, load_date 
			 from PVIDEO_EVENT 
			 where load_date < ?
             and client_name = ? and procedure_name = ? 
			   and event_code = 'ACTION_PLAY'
			   and video_current_time=0 ) pe2
		 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
	 join PURL_DOWNLOAD pu 		
		 on pe.shipment_id=pu.shipment_id and pe.doxee_platform_id=pu.doxee_platform_id			 
where sh.client_name = ?
and sh.procedure_name = ?
and pu.client_name = ? 
and pu.procedure_name = ? 
and pe2.load_date is null	
group by sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO, pu.user_agent;
