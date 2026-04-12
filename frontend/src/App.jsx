import { useState } from 'react'

// This imports your LoginPage component so you can use it here.
// The path './pages/LoginPage' means: go into the pages folder, get LoginPage.jsx
import LoginPage from './pages/LoginPage'
import BootPage from './pages/BootPage'
import Dashboard from './pages/Dashboard'

function App() {
  // This tracks which page to show.
  // 'login' means show the login page.
  // Later you will add more values like 'dashboard', 'expenses', etc.
  const [currentPage, setCurrentPage] = useState('boot')

  // This function will be called by LoginPage when login succeeds.
  // For now it just switches the page to a placeholder.
  function handleLoginSuccess() {
    setCurrentPage('dashboard')
  }

  // This decides which component to render based on currentPage.
  // Think of it as a switch statement for your UI.
  return (
      <div>

        {currentPage === 'boot' && (
            <BootPage onLogin={() => setCurrentPage('login')}
                      onRegister={() => setCurrentPage('dashboard')}/>
        )}

        {currentPage === 'login' && (
            <LoginPage onLoginSuccess={handleLoginSuccess}
                      goToBoot={() => setCurrentPage('boot')}/>
        )}

        {currentPage === 'dashboard' && (
            <Dashboard ></Dashboard>
        )}
      </div>
  )
}

export default App