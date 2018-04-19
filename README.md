# Spring Boot Batch Example

Database schema SQL statements
```sql
CREATE SCHEMA family;
CREATE TABLE IF NOT EXISTS family.person(id SERIAL NOT NULL, first_name TEXT NOT NULL, last_name TEXT NOT NULL, PRIMARY KEY (id));
INSERT INTO family.person(first_name, last_name) VALUES ('Volodymyr', 'Prokopyuk');
SELECT * FROM family.person;
```

Spring Batch Job Execution status SQL query
```sql
select ji.job_instance_id, ji.job_name, -- Job Instance
    je.job_execution_id, je.start_time, je.end_time, je.status, je.exit_code, je.exit_message, -- Job Execution
    jep.key_name, jep.type_cd, jep.long_val, jep.double_val, jep.string_val, jep.date_val, -- Job Parameters
    jec.short_context, jec.serialized_context, -- Job Execution Context
    se.step_execution_id, se.step_name, se.start_time, se.end_time, -- Step Execution
    se.read_count, se.write_count, se.commit_count, se.rollback_count, -- Step Execution counts
    se.status, se.exit_code, se.exit_message, -- Step Execution status
    sec.short_context, sec.serialized_context -- Step Execution Context
from batch_job_instance ji
    join batch_job_execution je on je.job_instance_id = ji.job_instance_id
    join batch_job_execution_params jep on jep.job_execution_id = je.job_execution_id
    join batch_job_execution_context jec on jec.job_execution_id = je.job_execution_id
    join batch_step_execution se on se.job_execution_id = je.job_execution_id
    join batch_step_execution_context sec on sec.step_execution_id = se.step_execution_id
order by se.step_execution_id desc
```

