import Configuration from "../ui/Configuration";

// Agrupa accesos rapidos a opciones de configuracion.
export default function ConfigModal() {
        return (
        <div className="astrais-modal-surface rounded-md p-2">
            <div className="astrais-modal-soft-surface rounded-md">
                {/* Opciones de seguridad mostradas como filas reutilizables */}
                <div className="flex flex-col gap-2 p-2">
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                    <Configuration title="Password & Security" />
                </div>
            </div>
            
        </div>
    )
}
