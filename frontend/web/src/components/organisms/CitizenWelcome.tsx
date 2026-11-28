export function CitizenWelcome({ fullName }: { fullName: string }) {
  return (
    <section className="card citizen-welcome">
      <h1>Welcome, {fullName}</h1>
      <p>Give your electronics a second life</p>
    </section>
  );
}
