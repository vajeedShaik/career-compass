import ReactMarkdown from "react-markdown";

interface RoadmapViewProps {
  content: string;
}

export function RoadmapView({ content }: RoadmapViewProps) {
  return (
    <div className="prose prose-sm max-w-none dark:prose-invert prose-headings:font-semibold prose-a:text-primary prose-a:no-underline hover:prose-a:underline">
      <ReactMarkdown>{content}</ReactMarkdown>
    </div>
  );
}
