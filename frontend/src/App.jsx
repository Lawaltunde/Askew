import { Routes, Route } from 'react-router-dom'
import Nav from './components/Nav'
import Home from './pages/Home'
import History from './pages/History'
import './App.css'

export default function App() {
  return (
    <div className="shell">
      <Nav />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/history" element={<History />} />
      </Routes>
    </div>
  )
}
