select jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO as version,
       count(distinct pe.shipment_id) as total_unique,
       min(pe.load_date) as data_from, max(pe.load_date) as data_to
from LOT lot, JOB jb, SHIPMENT sh, EMAIL_TRANSFER pe
where pe.load_date > ?
      and jb.client_name = ?
      and jb.procedure_name = ?
      and jb.environment_name = ?
      and jb.application_name = ?
      and (jb.job_id=lot.job_id and jb.doxee_platform_id=lot.doxee_platform_id)
      and (lot.lot_id=sh.lot_id and lot.doxee_platform_id=sh.doxee_platform_id)
      and (sh.shipment_id=pe.shipment_id and sh.doxee_platform_id=pe.doxee_platform_id)
group by jb.client_name, jb.procedure_name, jb.environment_name, jb.application_name, sh.REGISTER_LETTER_NUMBER_TO
