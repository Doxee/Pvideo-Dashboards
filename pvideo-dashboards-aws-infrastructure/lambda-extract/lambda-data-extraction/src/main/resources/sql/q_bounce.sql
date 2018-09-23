select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO as version,
     count(distinct pe.shipment_id) as total_unique, min(pe.load_date) as data_from, max(pe.load_date) as data_to
from SHIPMENT sh 
     join (select shipment_id, doxee_platform_id, load_date from EMAIL_DELIVERY_INTERMEDIATE where load_date between ? and ?
              and client_name = ? and procedure_name = ? and event_description not in ('Delivery notification')
              union all 
             select shipment_id, doxee_platform_id, load_date from PURL_DELIVERY_INTERMEDIATE where load_date between ? and ?
              and client_name = ? and procedure_name = ? and event_description not in ('Delivery notification') and event_description not in ('Download hyperlink')) pe
          on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
     left join
           (select shipment_id, doxee_platform_id, load_date from EMAIL_DELIVERY_INTERMEDIATE where load_date < ?
             and client_name = ? and procedure_name = ? and event_description not in ('Delivery notification')
            union all 
            select shipment_id, doxee_platform_id, load_date from PURL_DELIVERY_INTERMEDIATE where load_date < ?
             and client_name = ? and procedure_name = ? and event_description not in ('Delivery notification') and event_description not in ('Download hyperlink'))  pe2
         on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
where sh.client_name = ?
and sh.procedure_name = ?
and pe2.load_date is null
group by sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO;