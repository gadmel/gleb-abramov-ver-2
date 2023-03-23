import React from 'react'
import {Route, Routes, Navigate} from 'react-router-dom'
import axios from 'axios'
import Cookies from 'js-cookie'

import LandingPage from "../LandingPage/LandingPage";
import LegalNotice from "../LegalNotice/LegalNotice";
import Login from "../Login/Login";
import Register from "../Register/Register";
import Admin from "../Admin/Admin";

axios.interceptors.request.use(
	function (config) {
		return fetch('/api/csrf/').then(() => {
			config.headers['X-XSRF-TOKEN'] = Cookies.get('XSRF-TOKEN')
			return config
		})
	},
	function (error) {
		return Promise.reject(error)
	}
)

function App() {

	return (
		<div className="App">
			<Routes>
				<Route path="/" element={<LandingPage/>}/>
				<Route path="/legal" element={<LegalNotice/>}/>

				<Route path={"/login"} element={<Login/>}/>

				<Route path={"/secured/"} element={<Admin/>}/>
				<Route path={"/secured/register/"} element={<Register/>}/>

				<Route path="/*" element={<Navigate to="/"/>}/>

			</Routes>
		</div>
	)
}

export default App
