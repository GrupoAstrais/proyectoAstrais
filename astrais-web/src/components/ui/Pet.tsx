interface PetProps {
    url: string;
}
export default function Pet({url} : PetProps) {
    return (
        <div>
            <img src={url} alt="Pet" className="w-20 h-20  object-cover" />
        </div>
  )
}