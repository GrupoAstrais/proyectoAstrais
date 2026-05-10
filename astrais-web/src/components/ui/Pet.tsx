import AstraisMascot from './AstraisMascot'

interface PetProps {
    url?: string;
}
export default function Pet({url} : PetProps) {
    return (
        <div>
            <AstraisMascot assetUrl={url} alt="Pet" className="h-20 w-20 object-cover" />
        </div>
  )
}
