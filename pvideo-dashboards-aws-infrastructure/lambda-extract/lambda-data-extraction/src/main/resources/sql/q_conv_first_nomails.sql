select subqueryA.job_id,subqueryA.client_name,subqueryA.procedure_name, subqueryA.scope, subqueryA.REGISTER_LETTER_NUMBER_TO as version
,datediff(hours, first_click,subqueryA.conv_date) as date_hh, count(subqueryA.shipment_id) as total_unique
from
(
		select jb.job_id,jb.client_name , jb.procedure_name, jb.scope, sh.REGISTER_LETTER_NUMBER_TO,pe.shipment_id
			 ,min(pe.create_date) as conv_date 
		from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
			 join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id
			 join (select shipment_id, doxee_platform_id, create_date
			        from PVIDEO_EVENT where load_date  between ? and ?
                        and client_name = ? and procedure_name= ?
			            and upper(substring(video_element_id from 1 for 4))='CONV'
			          ) pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
		 ------------------------
				 left join
				   (select shipment_id, doxee_platform_id, load_date
				     from PVIDEO_EVENT where load_date < ?
                        and client_name = ? and procedure_name= ?
			            and upper(substring(video_element_id from 1 for 4))='CONV') pe2
				 on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
        where jb.client_name = ?
        and jb.procedure_name = ?
        and lot.client_name = ?
        and lot.procedure_name = ?
        and sh.client_name = ?
        and sh.procedure_name = ?
      and pe2.load_date is null
	  ----------------------
		group by jb.job_id,jb.client_name, jb.procedure_name, jb.scope, sh.REGISTER_LETTER_NUMBER_TO, pe.shipment_id
)subqueryA join (
select job_id,client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO, min(firstclick) as first_click
 from
  	(	   select jb.job_id,jb.client_name, jb.procedure_name, jb.scope, sh.REGISTER_LETTER_NUMBER_TO,pe.shipment_id, min(pe.create_date) as firstclick
      		from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
      			 join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id

              join (select shipment_id, doxee_platform_id, create_date, event_code
          	 		        from PVIDEO_EVENT where client_name = ? and procedure_name= ? and event_code='ACTION_PLAY' and video_current_time=0
          	 		        ) pe
          	 			  on pe.shipment_id=sh.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
        where jb.client_name = ?
        and jb.procedure_name = ?
        and lot.client_name = ?
        and lot.procedure_name = ?
        and sh.client_name = ?
        and sh.procedure_name = ?
          	------------------------
      		group by jb.job_id,jb.client_name, jb.procedure_name, jb.scope, sh.REGISTER_LETTER_NUMBER_TO, pe.shipment_id
  )
group by job_id,client_name, procedure_name, scope, REGISTER_LETTER_NUMBER_TO
) subqueryB on subqueryB.job_id=subqueryA.job_id and subqueryA.client_name=subqueryB.client_name and subqueryA.procedure_name=subqueryB.procedure_name and subqueryA.scope=subqueryB.scope
and subqueryA.REGISTER_LETTER_NUMBER_TO=subqueryB.REGISTER_LETTER_NUMBER_TO
where subqueryA.client_name = ?
and subqueryA.procedure_name = ?
group by subqueryA.job_id,subqueryA.client_name,subqueryA.procedure_name, subqueryA.scope, subqueryA.REGISTER_LETTER_NUMBER_TO, date_hh;