select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO as version,
   count(distinct pe.shipment_id) as total_unique, min(pe.load_date) as data_from, max(pe.load_date) as data_to
from SHIPMENT sh 
join (select id,shipment_id, doxee_platform_id, load_date, create_date
			        from purl where load_date between ? and ?
                    and client_name = ? and procedure_name = ?
			          ) pe
				  on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
  left join
           (
      select id,shipment_id, doxee_platform_id, load_date, create_date
			        from purl where load_date < ?
                    and client_name = ? and procedure_name = ?
			          ) pu
				  on sh.shipment_id=pu.shipment_id and sh.doxee_platform_id=pu.doxee_platform_id
where sh.client_name = ?
and sh.procedure_name = ?
and pu.load_date is null
group by sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO;