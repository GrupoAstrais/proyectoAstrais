import Configuration from "../ui/Configuration";

export default function ConfigModal() {
        return (
        <div className="astrais-modal-surface rounded-md p-2">
            <div className="astrais-modal-soft-surface rounded-md">
                <div className="flex flex-col gap-2 p-2">
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                </div>
            </div>
            
        </div>
    )
}
