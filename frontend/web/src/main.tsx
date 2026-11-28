import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<main><h1>REBOOT</h1><p>Reparación, donación y reciclaje de equipos electrónicos.</p></main>} />
        <Route path="*" element={<main><h1>Página no encontrada</h1><Link to="/">Volver al inicio</Link></main>} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
);
