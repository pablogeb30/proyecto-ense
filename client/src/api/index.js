import DATA from './data'

let __instance = null

export default class API {

	#url = 'http://localhost:8080'
	#token = localStorage.getItem('token') || null

	#formatSortObject(sortObject) {
		return Object.entries(sortObject)
			.map(([key, value]) => `${value}${key}`)
			.join(',');
	}

	static instance() {

		if(__instance == null) {

			__instance = new API()

		}

		return __instance

	}

	async login(email, pass) {

		let body = {
			"email": email,
			"password": pass
		}

		let response = await fetch(`${this.#url}/login`, {

			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(body)

		})

		if(response.ok) {

			let token = response.headers.get('authentication')

			localStorage.setItem('user', email)
			localStorage.setItem('token', token)
			this.#token = token

			localStorage.setItem('name', await this.findUser(email).name)

			return true

		} else {

			return false

		}

	}

	async logout() {

		this.#token = null
		localStorage.clear()

		return true

	}

	async findMovies(
		{
			filter: { genre = '', title = '', status = '' } = { genre : '', title : '', status : '' },
			sort,
			pagination: {page = 0, size = 7} = { page: 0, size: 7 }
		} = {
			filter: { genre : '', title : '', status : '' },
			sort: {},
			pagination: { page: 0, size: 7 }
		}
	) {

		let query = new URLSearchParams({
			"genres": genre,
			"status": status,
			"title": title,
			"sort": sort != null ? this.#formatSortObject(sort) : null,
			"page": page,
			"size": size
		})

		let response = await fetch(`${this.#url}/movies?${query}`, {

			method: 'GET',
			headers: {
				'Authorization': this.#token
			}

		})

		if(response.ok) {

			let data = await response.json()
			let movies = data.data

			let links = response.headers.get('link').split(',')
			let hasNext = false
			let hasPrevious = false

			let actualLink = links.find(link => link.includes('rel="self"')).split(';')[0].replace('<', '').replace('>', '').trim()
			let lastLink = links.find(link => link.includes('rel="last"')).split(';')[0].replace('<', '').replace('>', '').trim()
			let firstLink = links.find(link => link.includes('rel="first"')).split(';')[0].replace('<', '').replace('>', '').trim()

			if (actualLink !== lastLink) {
				hasNext = true
			}

			if (actualLink !== firstLink) {
				hasPrevious = true
			}

			let returnData = {
				content: movies,
				pagination: {
					hasNext: hasNext,
					hasPrevious: hasPrevious
				}
			}

			return returnData

		} else {

			return []

		}

	}

	async findMovie(id) {

		let response = await fetch(`${this.#url}/movies/${id}`, {

			method: 'GET',
			headers: {
				'Authorization': this.#token
			}

		})

		if(response.ok) {

			let data = await response.json()
			let movie = data.data

			return movie

		} else {

			return null

		}

	}

	async findUser(id) {

		let response = await fetch(`${this.#url}/users/${id}`, {

			method: 'GET',
			headers: {
				'Authorization': this.#token
			}

		})

		if(response.ok) {

			let data = await response.json()
			let user = data.data

			return user

		} else {

			return null

		}

	}

	async findComments(
		{
			filter: { movie = '', user = '' } = { movie: '', user: '' },
			sort,
			pagination: {page = 0, size = 3} = { page: 0, size: 3}
		} = {
			filter: { movie: '', user: '' },
			sort: {},
			pagination: { page: 0, size: 3}
		}
	) {

		if (movie == '' && user == '') {
			return { content: [], pagination: { hasNext: false, hasPrevious: false } }
		}

		let url

		if (movie) {
			url = `${this.#url}/movies/${movie}/assessments`
		} else {
			url = `${this.#url}/users/${user}/assessments`
		}

		let response = await fetch(url, {

			method: 'GET',
			headers: {
				'Authorization': this.#token,
				'Content-Type': 'application/json'
			}

		})

		if(response.ok) {

			let data = await response.json()
			let comments = data.data

			let links = response.headers.get('link').split(',')
			let hasNext = false
			let hasPrevious = false

			let actualLink = links.find(link => link.includes('rel="self"')).split(';')[0].replace('<', '').replace('>', '').trim()
			let lastLink = links.find(link => link.includes('rel="last"')).split(';')[0].replace('<', '').replace('>', '').trim()
			let firstLink = links.find(link => link.includes('rel="first"')).split(';')[0].replace('<', '').replace('>', '').trim()

			if (actualLink !== lastLink) {
				hasNext = true
			}

			if (actualLink !== firstLink) {
				hasPrevious = true
			}

			return { content: comments, pagination: { hasNext: hasNext, hasPrevious: hasPrevious } }

		} else {

			return { content: [], pagination: { hasNext: false, hasPrevious: false } }

		}

	}

	async createComment(comment) {

		let body = {
			"rating": comment.rating,
			"comment": comment.comment,
			"user": {
				"email": localStorage.getItem('user'),
				"name": localStorage.getItem('name')
			}
		}

		let response = await fetch(`${this.#url}/movies/${comment.id}/assessments`, {

			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'Authorization': this.#token
			},
			body: JSON.stringify(body)

		})

		return response.ok

	}

	async createUser(user) {

		let response = await fetch(`${this.#url}/users`, {

			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(user)

		})

		if(response.ok) {

			let data = await response.json()
			let user = data.data

			return user

		} else {

			return null

		}

	}

	async updateUser(id, user) {
		console.log(user)
	}
}