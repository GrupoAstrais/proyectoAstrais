import Configuration from "../ui/Configuration";

export default function ConfigModal() {
        return (
        <div className="bg-accent-beige-300 rounded-md p-2">
            <div className="bg-secondary-500 rounded-bd">
                <div className="flex flex-col gap-2 p-2">
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                </div>
            </div>
            
        </div>
    )
}