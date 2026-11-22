// Synthetic versions of the eight service-directory designs. Not institutional policy.
export const showcaseServices=[
  ['SVC-01','Research Registration',['Completed registration form','Research proposal','Adviser endorsement']],
  ['SVC-02','Ethics Review & Clearance',['Ethics application','Research protocol','Informed consent form']],
  ['SVC-03','CRM Clearance Processing',['Clearance application','Completion evidence']],
  ['SVC-04','UNIVERSITY Journal Endorsement',[],'external'],
  ['SVC-05','Research Consultation',[]],
  ['SVC-06','Statistical Report Generation',['Analysis request','Research instruments']],
  ['SVC-07','Manuscript Revision Processing',['Revised manuscript','Revision response matrix']],
  ['SVC-08','Repository Archiving',['Final manuscript','Archiving permission']]
];

export async function seedShowcaseServices(admin,sender,form){
  const {rows:[actor]}=await admin.query("SELECT user_id FROM crm_staff_permissions WHERE permission='content_management' ORDER BY user_id LIMIT 1");
  for(const [code,name,requirements,channel='in_app'] of showcaseServices){
    const {rows:[service]}=await admin.query('INSERT INTO crm_services(code) VALUES($1) RETURNING id',[code]);
    const details={name,description:'Synthetic presentation fixture: '+name,
      purpose:'Demonstrate the '+name.toLowerCase()+' workflow using synthetic records.',
      eligibility:'Verified demo student and faculty researchers.',applicants:['Student','Faculty'],
      contact_name:'Demo CRM Office',contact_email:sender,turnaround:'Synthetic demo; no institutional service commitment',channel,
      instructions:[channel==='external'?'Use the demo contact instructions; this service is handled outside the app.':'Complete the requirements and submit through the app.'],
      requirements:requirements.map(title=>({title,required:true}))};
    const {rows:[revision]}=await admin.query(`INSERT INTO crm_service_revisions
      (service_id,version,state,details,form_name,form_mime,form_bytes,form_updated_at,published_at)
      VALUES($1,1,'published',$2,'synthetic-demo-form.pdf','application/pdf',$3,now(),now()) RETURNING id`,[service.id,details,form]);
    await admin.query("INSERT INTO crm_service_history(revision_id,actor_id,action) VALUES($1,$2,'published')",[revision.id,actor.user_id]);
  }
}
