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

const darkColors = {
	neutral0: "#242424",
	neutral10: "#1a1a1a",
	neutral20: "#fff",
	neutral50: "#9a9a9a",
	neutral80: "#fff",
	primary: "#9ab5d4",
	primary25: "#213547",
	danger: "#fff",
	dangerLight: "#c21919",
}

const lightColors = {
	neutral0: "#fff",
	neutral10: "#9ab5d4",
	neutral20: "#213547",
	neutral50: "#9a9a9a",
	neutral80: "#213547",
	primary: '#213547',
	primary25: '#aecaea',
	danger: "#fff",
	dangerLight: "#c21919",
}

const selectTheme = (theme: any) => ({
	...theme,
	colors: theme === 'dark'
		? darkColors
		: lightColors,
})

export {selectStyles, selectTheme}
