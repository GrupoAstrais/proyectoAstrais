
export default function Login() {
  return (
    <>
        <div>
            <form action="">
                <h1>LOG INTO YOUR ACCOUNT</h1>
                <div>
                    <label>Username/Email</label>
                    <input type="text" placeholder="Username" required />
                </div>
                <div>
                    <label>Password</label>
                    <input type="paswword" placeholder="Password" required />
                    <a>Frogot?</a>
                </div>
                <div>
                    <label><input type="checkbox" />Remember me</label>
                </div>
                <button type="submit">Login</button>
                <button type="submit">Login with Google</button>
                <p>Don´t have an account? <a>Sign Up</a></p>
            </form>
        </div>
    </>
  )
}