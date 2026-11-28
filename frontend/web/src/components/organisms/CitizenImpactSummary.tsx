export function CitizenImpactSummary() {
  return (
    <section className="citizen-impact" aria-labelledby="citizen-impact-heading">
      <h2 id="citizen-impact-heading">Your Impact</h2>
      <dl className="card-grid">
        <div className="card"><dt>Devices Donated</dt><dd>0</dd></div>
        <div className="card"><dt>Devices Recycled</dt><dd>0</dd></div>
        <div className="card"><dt>CO₂ Saved</dt><dd>0 kg</dd></div>
      </dl>
    </section>
  );
}
