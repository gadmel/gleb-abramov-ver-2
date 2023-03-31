const selectStyles = {
	container: (provided: any) => ({
		...provided,
		width: '100%'
	}),
	singleValue: (provided: any) => ({
		...provided,
		textAlign: 'start'
	}),
	placeholder: (provided: any) => ({
		...provided,
		textAlign: 'start'
	}),
}

const wideScreenSelectStyles = {
	...selectStyles,
	container: (provided: any) => ({
		...provided,
		width: '50%'
	}),
}

const darkColors = {
	neutral0: "#242424",
	neutral20: "#fff",
	neutral50: "#9a9a9a",
	neutral80: "#fff",
	primary: "#9ab5d4",
	primary25: "#213547",
	danger: "#9b0d0d",
	dangerLight: "#c21919",
}

const lightColors = {
	neutral0: "#fff",
	neutral20: "#213547",
	neutral50: "#9a9a9a",
	neutral80: "#213547",
	primary: '#213547',
	primary25: '#aecaea',
	danger: "#9b0d0d",
	dangerLight: "#c21919",
}

const selectTheme = (theme: any) => ({
	...theme,
	colors: theme === 'dark'
		? darkColors
		: lightColors,
})

export {selectStyles, wideScreenSelectStyles, selectTheme}
