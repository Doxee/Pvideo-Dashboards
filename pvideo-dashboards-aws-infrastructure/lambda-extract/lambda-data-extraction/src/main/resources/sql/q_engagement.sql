select client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO as version, progress,
       count(distinct shipment_id) as total_unique, min(load_date) as data_from, max(load_date) as data_to
from (
	select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO,
		  pe.shipment_id, pe.load_date, pe.PROGRESS_INT as progress
		from SHIPMENT as sh
			 join (select shipment_id, doxee_platform_id, load_date,  
			         CASE 
                  WHEN PROGRESS <= 10 THEN 10
                  WHEN PROGRESS > 10 and PROGRESS <= 20 then 20
                  WHEN PROGRESS > 20 and PROGRESS <= 30 then 30
                  WHEN PROGRESS > 30 and PROGRESS <= 40 then 40
                  WHEN PROGRESS > 40 and PROGRESS <= 50 then 50
                  WHEN PROGRESS > 50 and PROGRESS <= 60 then 60
                  WHEN PROGRESS > 60 and PROGRESS <= 70 then 70
                  WHEN PROGRESS > 70 and PROGRESS <= 80 then 80
                  WHEN PROGRESS > 80 and PROGRESS <= 90 then 90
                  ELSE 100
                  END as PROGRESS_INT	
        			  from PVIDEO_EVENT where load_date between ? and ?
                      and client_name = ? and procedure_name = ?
                        and event_code='EVENT_PROGRESS_UPDATE') pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
			 left join
				   (select shipment_id, doxee_platform_id, load_date, 
				        CASE 
                    WHEN PROGRESS <= 10 THEN 10
                    WHEN PROGRESS > 10 and PROGRESS <= 20 then 20
                    WHEN PROGRESS > 20 and PROGRESS <= 30 then 30
                    WHEN PROGRESS > 30 and PROGRESS <= 40 then 40
                    WHEN PROGRESS > 40 and PROGRESS <= 50 then 50
                    WHEN PROGRESS > 50 and PROGRESS <= 60 then 60
                    WHEN PROGRESS > 60 and PROGRESS <= 70 then 70
                    WHEN PROGRESS > 70 and PROGRESS <= 80 then 80
                    WHEN PROGRESS > 80 and PROGRESS <= 90 then 90
                    ELSE 100
                    END as PROGRESS_INT	
				      from PVIDEO_EVENT where load_date < ?
                      and client_name = ? and procedure_name = ?
				       and event_code='EVENT_PROGRESS_UPDATE') pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
				   and pe.PROGRESS_INT= pe2.PROGRESS_INT
		where sh.client_name = ?
    and sh.procedure_name = ?  
		and pe2.load_date is null)	
group by client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO, progress;