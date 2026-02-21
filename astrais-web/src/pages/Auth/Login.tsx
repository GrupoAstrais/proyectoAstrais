
export default function Login() {
  return (
    <>
        <div>
            <form action="">
                <h1>Login</h1>
                <div>
                    <input type="text" placeholder="Username" required />
                </div>
                <div>
                    <input type="paswword" placeholder="Password" required />
                </div>
                <div>
                    <label><input type="checkbox" />Remember me</label>
                </div>
                <button type="submit">Login</button>
            </form>
        </div>
    </>
  )
}