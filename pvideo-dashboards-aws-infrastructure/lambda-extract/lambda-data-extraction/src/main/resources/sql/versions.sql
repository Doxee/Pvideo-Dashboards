select sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO as version       
from SHIPMENT sh
where  sh.client_name = ?
and sh.procedure_name = ?
and sh.load_date >= ?
and sh.load_date < ?
group by sh.client_name, sh.procedure_name, sh.scope, sh.REGISTER_LETTER_NUMBER_TO
;