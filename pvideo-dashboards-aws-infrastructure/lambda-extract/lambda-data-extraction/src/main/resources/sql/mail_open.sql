select jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO as version,
       count(distinct pe.shipment_id) as total_unique,
       min(pe.load_date) as data_from, max(pe.load_date) as data_to
from JOB jb join LOT lot on jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id
     join SHIPMENT sh on lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id
     join (select shipment_id, doxee_platform_id, load_date from EMAIL_OPENING_EVENT where load_date > ?) pe
     on sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id
     left join
     (select shipment_id, doxee_platform_id, load_date from EMAIL_OPENING_EVENT where load_date <= ?) pe2
     on sh.shipment_id=pe2.shipment_id and sh.doxee_platform_id=pe2.doxee_platform_id
where jb.client_name = ?
      and jb.procedure_name = ?
      and jb.environment_name = ?
      and jb.application_name = ?
      and pe2.load_date is null
group by jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO
