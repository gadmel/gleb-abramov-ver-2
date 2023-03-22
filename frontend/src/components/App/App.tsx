import React from 'react'
import {Route, Routes} from 'react-router-dom'

import LandingPage from "../LandingPage/LandingPage";

function App() {

	return (
		<div className="App">
			<main className="main">
				<Routes>
					<Route path="/" element={<LandingPage/>}/>
				</Routes>
			</main>
		</div>
	)
}

export default App
