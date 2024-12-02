import { useEffect, useState } from 'react'

import API from '../api'

export function useMovies(query = {}) {

	const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
	const queryString = JSON.stringify(query)

	useEffect(() => {
		API.instance()
			.findMovies(JSON.parse(queryString))
			.then(setData)
	}, [queryString])

	return data

}

export function useMovie(id = '') {

	const [status, setStatus] = useState()
	const [data, setData] = useState({})

	useEffect(() => {
		API.instance()
			.findMovie(id)
			.then(setData)
	}, [id])

	const update = (id, movie) => API.instance()
		.updateMovie(id, movie)
		.then(movie => {
			if (movie) {
				setData(movie)
				setStatus(0)
			} else {
				setStatus(-1)
			}
		}
	)

	return {
		movie: data,
		status,
		update
	}

}

export function useUser(id = null) {

	const [data, setData] = useState([])
	const userId = id === null ? localStorage.getItem('user') : id
	const [status, setStatus] = useState()

	useEffect(() => {

		if (userId === null) {
			return
		}

		API.instance().findUser(userId).then(user => {
			setData(user)
		})

	}, [userId])

	const create = user => API.instance()
			.createUser(user)
			.then(user => setData(user))

	const update = (operations) => API.instance()
			.updateUser(userId, operations)
			.then(user => {
				if (user) {
					setData(user)
					setStatus(0)
				} else {
					setStatus(-1)
				}
			})

	const updateFriend = (friendId, operations) => API.instance()
		.updateFriend(userId, friendId, operations)
		.then(user => setData(user))

	const deleteFriend = (friendId) => API.instance()
		.deleteFriend(userId, friendId)
		.then(user => setData(user))

	return {
		user: data,
		status,
		create,
		update,
		updateFriend,
		deleteFriend
	}

}

export function useComments(query = {}){

	const [data, setData] = useState({ content: [], pagination: { hasNext: false, hasPrevious: false }})
	const queryString = JSON.stringify(query)

	useEffect(() => {

		if (Object.keys(query).length === 0) {
			return
		}

		API.instance()
			.findComments(JSON.parse(queryString))
			.then(setData)

	}, [queryString])

	const create = comment => {
		API.instance()
			.createComment(comment)
			.then( () => {
				API.instance()
					.findComments(query)
					.then(setData)
			})
	}

	return {
		comments: data,
		createComment: create
	}

}