import reactLogo from './assets/react.svg'
import './App.css'
import axios from "axios";

function App() {
 const handleFetchApi = () => {
     axios.get('/api/hello/').then((res) => {
         alert(res.data)
     })
 }
  return (
    <div className="App">
      <div>
        <a href="https://vitejs.dev" target="_blank">
          <img src="/vite.svg" className="logo" alt="Vite logo" />
        </a>
        <a href="https://reactjs.org" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <div className="card">
        <p>
          This build was successfully deployed triggered by Google Cloud Build trigger on push to main branch.
        </p>
         <button onClick={handleFetchApi}>
          Say "Hello" to the API
        </button>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </div>
  )
}

export default App
